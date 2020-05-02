package controller.paradigm.eventloop;

import controller.Controller;
import controller.PartialWikiGraph;
import controller.paradigm.concurrent.PartialWikiGraphImpl;
import controller.api.RESTWikiGraph;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;
import view.ViewEvent.EventType;

import java.util.Locale;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class EventLoopController implements Controller {

    private final View view;
    private final Vertx vertx;
    private String language = Locale.ENGLISH.getLanguage();
    private PartialWikiGraph last;
    private boolean toClear;
    private Lock mutex = new ReentrantLock();

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
                    if (this.last == null) {
                        this.view.clearGraph();
                    } else {
                        this.toClear = true;
                        this.last.setAborted();
                    }
                } finally {
                    mutex.unlock();
                }
                break;
            case SEARCH:
                startComputing(event.getText(), event.getDepth());
                break;
            case RANDOM_SEARCH:
                startComputing(null, event.getDepth());
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
        }
    }

    private void startComputing(String term, int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        final PartialWikiGraph graph = new PartialWikiGraphImpl();
        mutex.lock();
        try {
            if (this.last != null) {
                this.last.setAborted();
            }
            this.last = graph;
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
                                if (this.last == graph) {
                                    if (toClear) {
                                        this.view.clearGraph();
                                        System.out.println(Thread.currentThread().getName() + " cleared");
                                        this.toClear = false;
                                    }
                                    this.last = null;
                                }
                            } finally {
                                mutex.unlock();
                            }
                        }).compute();
                    } else {
                        System.err.println(result.cause());
                    }
                });
    }

    @Override
    public void start() {
        view.start();
    }
}
