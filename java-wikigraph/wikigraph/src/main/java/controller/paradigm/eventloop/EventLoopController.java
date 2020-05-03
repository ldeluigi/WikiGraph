package controller.paradigm.eventloop;

import controller.Controller;
import controller.PartialWikiGraph;
import controller.api.RESTWikiGraph;
import controller.paradigm.concurrent.PartialWikiGraphImpl;
import controller.update.GraphAutoUpdateRequest;
import controller.update.NoOpView;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.WikiGraph;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;

import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventLoopController implements Controller {

    private final View view;
    private final Vertx vertx;
    private String language = Locale.ENGLISH.getLanguage();
    private PartialWikiGraph graphBeingComputed;
    private boolean toClear;
    private final Lock mutex = new ReentrantLock();

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
                } finally {
                    mutex.unlock();
                }
                break;
            case SEARCH:
                startComputing(event.getText(), event.getInt());
                break;
            case RANDOM_SEARCH:
                startComputing(null, event.getInt());
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
                    } else if (event.getInt() > 0) {
                        this.updateDelay = event.getInt();
                        System.out.println("UPDATED DELAY");
                        if (!this.autoUpdate) {
                            this.autoUpdate = true;
                            System.out.println("ON AUTO UPDATE");
                            if (this.graphBeingComputed == null && this.autoUpdateReq != null) {
                                if (this.isUpdating) throw new IllegalStateException("is Updating left true");
                                System.out.println("STARTING AUTO UPDATE");
                                startAutoUpdating(this.autoUpdateReq.getOriginal().getRoot());
                            }
                        }
                    }
                } finally {
                    mutex.unlock();
                }
                break;
        }
    }

    private void startAutoUpdating(final String forRoot) {
        final PartialWikiGraph graph = new PartialWikiGraphImpl();
        mutex.lock();
        try {
            final String root = this.autoUpdateReq.getOriginal().getRoot();
            if (this.autoUpdateReq == null || !this.autoUpdateReq.getOriginal().getRoot().equals(forRoot)) {
                System.out.println(forRoot + " UPDATE CANCELED");
                return;
            }
            this.graphBeingComputed = graph;
            this.isUpdating = true;
            final WikiGraph old = this.autoUpdateReq.getOriginal();
            final WikiGraphNodeFactory fac = this.autoUpdateReq.getNodeFactory();
            final int maxDepth = this.autoUpdateReq.getDepth();
            this.vertx.runOnContext(new VertxNodeRecursion(this.vertx,
                    fac,
                    graph,
                    new NoOpView(),
                    maxDepth,
                    root, () -> {
                mutex.lock();
                try {
                    if (graphBeingComputed == graph && !graph.isAborted()) {
                        System.out.println("CALCULATING DIFFERENCES FOR " + root);
                        /*
                            HERE COMPUTE DIFFERENCES
                         */
                        this.graphBeingComputed = null;
                        if (this.autoUpdate) {
                            System.out.println("RESCHEDULING UPDATE FOR " + root);
                            this.autoUpdateReq.setOriginal(graph);
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
            }));
        } finally {
            mutex.unlock();
        }
    }

    private void startComputing(String term, int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        final PartialWikiGraph graph = new PartialWikiGraphImpl();
        mutex.lock();
        try {
            if (this.graphBeingComputed != null) {
                System.out.println("START COMPUTING ABORTED LAST");
                this.graphBeingComputed.setAborted();
            }
            this.graphBeingComputed = graph;
            this.isUpdating = false;
            this.autoUpdateReq = new GraphAutoUpdateRequest(graph, nodeFactory, depth, term);
        } finally {
            mutex.unlock();
        }

        this.vertx.executeBlocking(p -> {
                    if (nodeFactory.setLanguage(this.language)) {
                        p.complete();
                    } else {
                        p.fail("Language doesn't exist, aborting");
                    }
                },
                result -> {
                    if (result.succeeded()) {
                        new VertxNodeRecursion(this.vertx, nodeFactory, graph, this.view, depth, term, () -> {
                            mutex.lock();
                            try {
                                if (this.graphBeingComputed == graph) {
                                    System.out.println(term + " IS LAST");
                                    this.graphBeingComputed = null;
                                    if (toClear) {
                                        System.out.println(term + " CLEARS");
                                        this.view.clearGraph();
                                        this.autoUpdateReq = null;
                                        System.out.println(Thread.currentThread().getName() + " cleared");
                                        this.toClear = false;
                                    } else if (autoUpdate && !graph.isAborted()) {
                                        System.out.println(term + " STARTS AUTO UPDATING");
                                        startAutoUpdating(graph.getRoot());
                                    }
                                } else {
                                    System.out.println(term + " QUIETLY SHUTS DOWN");
                                }
                            } finally {
                                mutex.unlock();
                            }
                        }).compute();
                    } else {
                        System.err.println(result.cause().getMessage());
                    }
                });
    }

    @Override
    public void start() {
        view.start();
    }
}
