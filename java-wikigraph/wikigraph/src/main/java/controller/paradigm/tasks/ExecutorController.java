package controller.paradigm.tasks;


import controller.Controller;
import controller.api.RESTWikiGraph;
import controller.paradigm.concurrent.ConcurrentWikiGraph;
import controller.paradigm.concurrent.SynchronizedWikiGraph;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;
import view.ViewEvent.EventType;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorController implements Controller {

    private final View view;
    private final Lock scheduleLock = new ReentrantLock();
    private ForkJoinPool pool;
    private Optional<ViewEvent> event = Optional.empty();
    private ConcurrentWikiGraph last = null;
    private final AtomicReference<String> language = new AtomicReference<>(Locale.ENGLISH.getLanguage());
    private boolean isQuiescent = true;

    public ExecutorController(final View view) {
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start() {
        this.pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 5);
        this.view.start();
    }

    private void startComputing(final String root, final int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        this.pool.execute(() -> {
            nodeFactory.setLanguage(language.get());
            ConcurrentWikiGraph graph = SynchronizedWikiGraph.empty();
            scheduleLock.lock();
            try {
                this.last = graph;
            } finally {
                scheduleLock.unlock();
            }
            new ComputeChildrenTask(nodeFactory, graph, this.view, depth, root) {
                @Override
                public void onCompletion(CountedCompleter<?> caller) {
                    super.onCompletion(caller);
                    scheduleLock.lock();
                    try {
                        if (last == graph){
                            if (event.isPresent()) {
                                resolveEvent(event.get());
                                event = Optional.empty();
                            } else {
                                isQuiescent = true;
                            }
                            last = null;
                        }
                    } finally {
                        scheduleLock.unlock();
                    }
                }
            }.compute();
        });
    }

    private void exit() {
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }

    private void resolveEvent(final ViewEvent event){
        switch (event.getType()) {
            case EXIT:
                this.exit();
                event.onComplete(true);
                break;
            case SEARCH:
                startComputing(event.getText(), event.getDepth());
                event.onComplete(true);
                break;
            case RANDOM_SEARCH:
                startComputing(null, event.getDepth());
                event.onComplete(true);
                break;
            case CLEAR:
                this.view.clearGraph();
                this.isQuiescent = true;
                event.onComplete(true);
                break;
            case LANGUAGE:
                this.pool.execute(() -> {
                    if (new RESTWikiGraph().setLanguage(event.getText())) {
                        this.language.set(event.getText());
                        event.onComplete(true);
                    } else {
                        event.onComplete(false);
                    }
                });
                this.isQuiescent = true;
                break;
        }
    }

    @Override
    public void notifyEvent(final ViewEvent event) {
        this.scheduleLock.lock();
        try {
            if (this.last != null) {
                this.last.setAborted();
            }
            if (this.isQuiescent) {
                this.isQuiescent = false;
                this.resolveEvent(event);
            } else {
                this.event = Optional.of(event);
            }
        } finally {
            this.scheduleLock.unlock();
        }
    }

}
