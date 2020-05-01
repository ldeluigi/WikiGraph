package controller.paradigm.tasks;


import controller.Controller;
import controller.api.RESTWikiGraph;
import controller.paradigm.concurrent.ConcurrentWikiGraph;
import controller.paradigm.concurrent.SynchronizedWikiGraph;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;

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
        this.pool.execute(() -> {
            nodeFactory.setLanguage(language.get());
            this.last = SynchronizedWikiGraph.empty();
            this.pool.execute(new ComputeChildrenTask(nodeFactory, this.last, this.view, depth, root) {
                @Override
                public void onCompletion(CountedCompleter<?> caller) {
                    super.onCompletion(caller);
                    endComputing();
                }
            });
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
                e.onComplete(true);
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
            event.onComplete(true);
        } else if (event.getType().equals(ViewEvent.EventType.SEARCH)) {
            this.resolve(() -> {
                        startComputing(event.getText(), event.getDepth());
                        event.onComplete(true);
                    },
                    () -> this.event = Optional.of(event));
        } else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)) {
            this.resolve(() -> {
                        startComputing(null, event.getDepth());
                        event.onComplete(true);
                    },
                    () -> this.event = Optional.of(event));
        } else if (event.getType().equals(ViewEvent.EventType.CLEAR)) {
            this.resolve(() -> {
                        this.view.clearGraph();
                        event.onComplete(true);
                    },
                    () -> this.event = Optional.of(event));
        } else if (event.getType().equals(ViewEvent.EventType.LANGUAGE)) {
            this.pool.execute(() -> {
                if (new RESTWikiGraph().setLanguage(event.getText())) {
                    this.language.set(event.getText());
                    event.onComplete(true);
                } else {
                    event.onComplete(false);
                }
            });
        }
    }

    private void resolve(final Runnable quiescentBranch, final Runnable nonQuiescentBranch) {
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
