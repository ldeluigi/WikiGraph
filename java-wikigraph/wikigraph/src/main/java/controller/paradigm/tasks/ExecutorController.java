package controller.paradigm.tasks;


import controller.ConcurrentWikiGraph;
import controller.Controller;
import controller.SynchronizedWikiGraph;
import controller.api.RESTWikiGraph;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorController implements Controller {

    private final View view;
    private final Lock scheduleLock = new ReentrantLock();
    private ForkJoinPool pool;
    private Optional<ViewEvent> event = Optional.empty();
    private ConcurrentWikiGraph last = null;

    public ExecutorController(final View view) {
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start() {
        pool = ForkJoinPool.commonPool();
        view.start();
    }

    private void startComputing(final String root, final int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        this.last = SynchronizedWikiGraph.empty();
        this.pool.execute(new ComputeChildrenTask(nodeFactory, this.last, this.view, depth, root) {
            @Override
            public void onCompletion(CountedCompleter<?> caller) {
                super.onCompletion(caller);
                endComputing();
            }
        });
    }

    private void endComputing() {
        this.scheduleLock.lock();
        try {
            if (this.event.isPresent()) {
                final ViewEvent e = this.event.get();
                if (e.getType().equals(ViewEvent.EventType.CLEAR)) {
                    this.view.clearGraph();
                } else {
                    startComputing(e.getType().equals(ViewEvent.EventType.RANDOM_SEARCH) ? null : e.getText(),
                            e.getDepth());
                }
            }
        } finally {
            scheduleLock.unlock();
        }
    }

    private void exit() {
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }

    @Override
    public void notifyEvent(final ViewEvent event) {
        if (event.getType().equals(ViewEvent.EventType.EXIT)) {
            this.exit();
        } else if (event.getType().equals(ViewEvent.EventType.SEARCH)) {
            this.resolve(()-> startComputing(event.getText(), event.getDepth()), ()-> this.event = Optional.of(event));
        } else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)) {
            this.resolve(()-> startComputing(null, event.getDepth()), ()-> this.event = Optional.of(event));
        } else if (event.getType().equals(ViewEvent.EventType.CLEAR)) {
            this.resolve(()-> this.view.clearGraph(), ()-> this.event = Optional.of(event));
        }
    }

    private void resolve(final Runnable quiescentBranch, final Runnable nonQuiescentBranch){
        if (this.last != null) {
            this.last.setAborted();
            this.last = null;
        }
        this.scheduleLock.lock();
        try {
            if (this.pool.isQuiescent()) {
                quiescentBranch.run();
            } else {
                nonQuiescentBranch.run();
            }
        } finally {
            this.scheduleLock.unlock();
        }
    }

}
