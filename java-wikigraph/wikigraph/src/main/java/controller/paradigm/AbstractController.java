package controller.paradigm;

import controller.Controller;
import controller.api.RESTWikiGraph;
import controller.graphstream.GraphDisplaySink;
import controller.graphstream.OrderedGraphDiff;
import controller.update.GraphAutoUpdateRequest;
import controller.utils.WikiGraphManager;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public abstract class AbstractController implements Controller {
    private final View view;
    private final Lock mutex = new ReentrantLock();
    private final AtomicReference<String> language = new AtomicReference<>(Locale.ENGLISH.getLanguage());
    private WikiGraphManager graphBeingComputed;
    private boolean toClear;
    private GraphAutoUpdateRequest autoUpdateReq;
    private boolean isUpdating = false;
    private boolean autoUpdate = false;
    private int updateDelay;

    public AbstractController(View view) {
        this.view = view;
        view.addEventListener(this);
    }

    @Override
    public void start() {
        view.start();
    }

    @Override
    public void notifyEvent(final ViewEvent event) {
        switch (event.getType()) {
            case EXIT:
                this.exit();
                event.onComplete(true);
                break;
            case CLEAR:
                mutex.lock();
                try {
                    if (this.graphBeingComputed == null) {
                        System.out.println("CLEAR NOW");
                        this.view.clearGraph();
                        this.autoUpdateReq = null;
                    } else if (this.isUpdating) {
                        System.out.println("CLEAR NOW AND ABORT UPDATE");
                        this.graphBeingComputed.setAborted();
                        this.graphBeingComputed = null;
                        this.view.clearGraph();
                    } else {
                        System.out.println("SCHEDULE CLEAR FOR LATER");
                        this.toClear = true;
                        this.graphBeingComputed.setAborted();
                    }
                    event.onComplete(true);
                } finally {
                    mutex.unlock();
                }
                break;
            case SEARCH:
                startComputing(event.getText(), event.getInt());
                event.onComplete(true);
                break;
            case RANDOM_SEARCH:
                startComputing(null, event.getInt());
                event.onComplete(true);
                break;
            case LANGUAGE:
                this.checkLanguage(event.getText(),
                        () -> {
                            this.language.set(event.getText());
                            event.onComplete(true);
                        },
                        () -> event.onComplete(false));
                break;
            case AUTO_UPDATE:
                mutex.lock();
                try {
                    if (event.getInt() < 0 && this.autoUpdate) {
                        this.autoUpdate = false;
                        System.out.println("OFF AUTO UPDATE");
                        if (this.graphBeingComputed != null && this.isUpdating) {
                            System.out.println("ABORTING LAST");
                            this.graphBeingComputed.setAborted();
                            this.graphBeingComputed = null;
                            this.isUpdating = false;
                        }
                    } else if (event.getInt() >= 0) {
                        this.updateDelay = event.getInt();
                        System.out.println("UPDATED DELAY");
                        if (!this.autoUpdate) {
                            this.autoUpdate = true;
                            System.out.println("ON AUTO UPDATE");
                            if (this.graphBeingComputed == null && this.autoUpdateReq != null) {
                                System.out.println("STARTING AUTO UPDATE");
                                startAutoUpdating(this.autoUpdateReq.getOriginal().getRootID());
                            }
                        }
                    }
                    event.onComplete(true);
                } finally {
                    mutex.unlock();
                }
                break;
        }
    }

    protected abstract void checkLanguage(String language, Runnable success, Runnable failure);

    protected abstract void exit();

    private void startComputing(String term, int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        final WikiGraphManager graph = wikiGraphManager();
        graph.setGraphDisplay(this.view);
        mutex.lock();
        try {
            if (this.graphBeingComputed != null) {
                System.out.println("START COMPUTING ABORTED LAST");
                this.graphBeingComputed.setAborted();
            }
            this.graphBeingComputed = graph;
            this.isUpdating = false;
            this.autoUpdateReq = new GraphAutoUpdateRequest(nodeFactory, depth, graph);
        } finally {
            mutex.unlock();
        }

        this.computeAsync(nodeFactory,
                graph,
                depth,
                term,
                this.language.get(),
                this::onComputeComplete,
                () -> {
                    clearIfShould();
                    System.out.println("SEARCH ABORTED");
                });
    }

    protected abstract WikiGraphManager wikiGraphManager();

    protected abstract void computeAsync(WikiGraphNodeFactory nodeFactory, WikiGraphManager graph, int depth, String term, String language, Consumer<WikiGraphManager> onComputeComplete, Runnable failure);

    private void onComputeComplete(final WikiGraphManager graph) {
        mutex.lock();
        try {
            if (this.graphBeingComputed == graph) {
                System.out.println(graph.getRootID() + " IS LAST");
                this.graphBeingComputed = null;
                if (!clearIfShould() && autoUpdate && !graph.isAborted()) {
                    System.out.println(graph.getRootID() + " STARTS AUTO UPDATING");
                    startAutoUpdating(graph.getRootID());
                }
            } else {
                System.out.println(graph.getRootID() + " QUIETLY SHUTS DOWN");
            }
        } finally {
            mutex.unlock();
        }
    }

    private boolean clearIfShould() {
        if (toClear) {
            this.view.clearGraph();
            this.autoUpdateReq = null;
            System.out.println(Thread.currentThread().getName() + " cleared");
            this.toClear = false;
            return true;
        }
        return false;
    }

    private void startAutoUpdating(final String forRoot) {
        final WikiGraphManager graph = wikiGraphManager();
        mutex.lock();
        try {
            if (this.autoUpdateReq == null || !this.autoUpdateReq.getOriginal().getRootID().equals(forRoot)) {
                System.out.println(forRoot + " UPDATE CANCELED");
                return;
            }
            final String root = this.autoUpdateReq.getOriginal().getRootID();
            this.graphBeingComputed = graph;
            this.isUpdating = true;
            final WikiGraphNodeFactory factory = this.autoUpdateReq.getNodeFactory();
            final int maxDepth = this.autoUpdateReq.getDepth();
            this.computeAsync(
                    factory,
                    graph,
                    maxDepth,
                    root,
                    this.language.get(),
                    this::onAutoUpdateComplete,
                    () -> System.out.println(root + "UPDATES ABORTED"));
        } finally {
            mutex.unlock();
        }
    }


    private void onAutoUpdateComplete(final WikiGraphManager graph) {
        final String root = graph.getRootID();
        mutex.lock();
        try {
            if (graphBeingComputed == graph) {
                System.out.println("CALCULATING DIFFERENCES FOR " + root);
                final OrderedGraphDiff diff = new OrderedGraphDiff(this.autoUpdateReq.getOriginal().graph(),
                        graph.graph());
                diff.apply(new GraphDisplaySink(this.view,
                        this.autoUpdateReq.getOriginal().graph(),
                        graph.graph(),
                        this.autoUpdateReq.getNodeFactory().getLanguage()));
                this.graphBeingComputed = null;
                if (this.autoUpdate) {
                    System.out.println("RESCHEDULING UPDATE FOR " + root);
                    this.autoUpdateReq.updateOriginal(graph);
                    this.schedule(this.updateDelay, () -> startAutoUpdating(root));
                } else {
                    System.out.println("SHUT OFF ");
                    this.isUpdating = false;
                }
            } else {
                System.out.println(root + "UPDATES QUIETLY SHUTDOWN");
            }
        } finally {
            mutex.unlock();
        }
    }

    protected abstract void schedule(int updateDelay, Runnable autoUpdate);
}
