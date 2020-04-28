package controller;


import controller.api.HttpWikiGraph;
import controller.api.RESTWikiGraph;
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
    private HttpWikiGraph nodeFactory;
    private Optional<ViewEvent> event = Optional.empty();
    private ConcurrentWikiGraph last = null;

    public ExecutorController(View view) {
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start() {
        this.nodeFactory = new RESTWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        pool = ForkJoinPool.commonPool();
        view.start();
    }

    private void startComputing(String node, int depth, final ConcurrentWikiGraph last) {
        this.pool.execute(new ComputeChildrenTask(null, node, 0, this.nodeFactory, last, this.view, depth) {
            @Override
            public void onCompletion(CountedCompleter<?> caller) {
                super.onCompletion(caller);
                scheduleLock.lock();
                try {
                    if (event.isPresent()) {
                        final ViewEvent e = event.get();
                        ExecutorController.this.last = SynchronizedWikiGraph.empty();
                        startComputing(e.getType().equals(ViewEvent.EventType.RANDOM_SEARCH) ? null : e.getText(),
                                e.getDepth(), ExecutorController.this.last);
                    }
                } finally {
                    scheduleLock.unlock();
                }
            }
        });
    }

    private void exit() {
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }

    @Override
    public void notifyEvent(ViewEvent event) {
        if (event.getType().equals(ViewEvent.EventType.EXIT)) {
            this.exit();
        } else if (event.getType().equals(ViewEvent.EventType.SEARCH)) {
            if (this.last != null) {
                this.last.setAborted();
                this.last = null;
            }
            this.scheduleLock.lock();
            try {
                if (this.pool.isQuiescent()) {
                    this.last = SynchronizedWikiGraph.empty();
                    startComputing(event.getText(), event.getDepth(), this.last);
                } else {
                    this.event = Optional.of(event);
                }
            } finally {
                this.scheduleLock.unlock();
            }
        } else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)) {
            if (this.last != null) {
                this.last.setAborted();
                this.last = null;
            }
            this.scheduleLock.lock();
            try {
                if (this.pool.isQuiescent()) {
                    this.last = SynchronizedWikiGraph.empty();
                    startComputing(null, event.getDepth(), this.last);
                } else {
                    this.event = Optional.of(event);
                }
            } finally {
                this.scheduleLock.unlock();
            }
        }
    }

}
