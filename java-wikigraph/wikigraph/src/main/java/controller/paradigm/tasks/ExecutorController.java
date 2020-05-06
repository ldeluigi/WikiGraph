package controller.paradigm.tasks;


import controller.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import model.WikiGraphNodeFactory;
import view.View;

import java.io.IOException;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Task oriented implementation of an {@link AbstractController} using the {@link ScheduledExecutorService} and
 * {@link CountedCompleter}.
 */
public class ExecutorController extends AbstractController {

    private ScheduledExecutorService pool;

    /**
     * Creates an {@link ExecutorController}  that uses a {@link ScheduledThreadPoolExecutor}.
     *
     * @param view the view where data is displayed
     */
    public ExecutorController(final View view) {
        super(view);
    }

    @Override
    public void start() {
        this.pool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 5);
        super.start();
    }

    @Override
    protected void exit() {
        this.pool.shutdown();
    }

    @Override
    protected WikiGraphManager wikiGraphManager() {
        return SynchronizedWikiGraphManager.threadSafe();
    }

    @Override
    protected void checkLanguage(String language, Runnable success, Runnable failure) {
        this.pool.execute(() -> {
            try {
                if (new RESTWikiGraph().setLanguage(language)) {
                    success.run();
                } else {
                    failure.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void computeAsync(WikiGraphNodeFactory nodeFactory, WikiGraphManager graph, int depth, String term,
                                String language, Runnable onComputeComplete, Runnable failure) {
        this.pool.execute(() -> {
            try {
                nodeFactory.setLanguage(language);
            } catch (IOException e) {
                failure.run();
            }
            new ComputeChildrenTask(nodeFactory, graph, depth, term) {
                @Override
                public void onCompletion(CountedCompleter<?> caller) {
                    if (graph.isAborted()) {
                        failure.run();
                    } else {
                        onComputeComplete.run();
                    }
                }

                @Override
                public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
                    System.out.println("EXCEPTIONAL COMPLETION");
                    failure.run();
                    return false;
                }
            }.compute();
        });
    }

    @Override
    protected void schedule(int updateDelay, Runnable autoUpdate) {
        this.pool.schedule(autoUpdate, updateDelay, TimeUnit.MILLISECONDS);
    }
}
