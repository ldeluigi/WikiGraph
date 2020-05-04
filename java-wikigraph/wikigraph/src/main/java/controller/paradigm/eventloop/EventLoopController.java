package controller.paradigm.eventloop;

import controller.Controller;
import controller.graphstream.GraphDisplaySink;
import controller.graphstream.OrderedGraphDiff;
import controller.update.GraphAutoUpdateRequest;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import controller.api.RESTWikiGraph;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;

import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class EventLoopController implements Controller {

    private final View view;
    private final Vertx vertx;
    private final Lock mutex = new ReentrantLock();
    private String language = Locale.ENGLISH.getLanguage();
    private WikiGraphManager graphBeingComputed;
    private boolean toClear;
    private GraphAutoUpdateRequest autoUpdateReq;
    private boolean isUpdating = false;
    private boolean autoUpdate = false;
    private int updateDelay;

    public EventLoopController(View view) {
        this.view = view;
        view.addEventListener(this);
        this.vertx = Vertx.vertx();
    }

    @Override
    public void start() {
        view.start();
    }

    @Override
    public void notifyEvent(final ViewEvent event) {
        switch (event.getType()) {
            case EXIT:
                this.vertx.close();
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
                this.vertx.executeBlocking((Handler<Promise<String>>) p -> {
                    if (new RESTWikiGraph().setLanguage(event.getText())) {
                        p.complete(event.getText());
                    } else {
                        p.fail("Language doesn't exist");
                    }
                }, result -> {
                    if (result.succeeded()) {
                        language = result.result();
                        event.onComplete(true);
                    } else {
                        event.onComplete(false);
                    }
                });
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


    private void startComputing(String term, int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        final WikiGraphManager graph = SynchronizedWikiGraphManager.empty();
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

        checkLanguageAsync(nodeFactory,
                result -> {
                    if (result) {
                        new VertxNodeRecursion(this.vertx,
                                nodeFactory,
                                graph,
                                depth,
                                term,
                                () -> onComputeComplete(graphBeingComputed)
                        ).compute();
                    } else {
                        System.err.println("Language does not exist");
                    }
                }
        );

    }

    private void checkLanguageAsync(final WikiGraphNodeFactory nodeFactory, final Consumer<Boolean> onResult) {
        this.vertx.executeBlocking(p -> {
                    if (nodeFactory.setLanguage(this.language)) {
                        p.complete();
                    } else {
                        p.fail("Language doesn't exist, aborting");
                    }
                },
                result -> onResult.accept(result.succeeded()));
    }

    private void onComputeComplete(final WikiGraphManager graph) {
        mutex.lock();
        try {
            if (this.graphBeingComputed == graph) {
                System.out.println(graph.getRootID() + " IS LAST");
                this.graphBeingComputed = null;
                if (toClear) {
                    System.out.println(graph.getRootID() + " CLEARS");
                    this.view.clearGraph();
                    this.autoUpdateReq = null;
                    System.out.println(Thread.currentThread().getName() + " cleared");
                    this.toClear = false;
                } else if (autoUpdate && !graph.isAborted()) {
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

    private void startAutoUpdating(final String forRoot) {
        final WikiGraphManager graph = SynchronizedWikiGraphManager.empty();
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
            this.vertx.runOnContext(new VertxNodeRecursion(
                    this.vertx,
                    factory,
                    graph,
                    maxDepth,
                    root,
                    () -> onAutoUpdateComplete(graph)));
        } finally {
            mutex.unlock();
        }
    }


    private void onAutoUpdateComplete(final WikiGraphManager graph) {
        final String root = graph.getRootID();
        mutex.lock();
        try {
            if (graphBeingComputed == graph && !graph.isAborted()) {
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
                    this.vertx.setTimer(this.updateDelay, p -> startAutoUpdating(root));
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
}
