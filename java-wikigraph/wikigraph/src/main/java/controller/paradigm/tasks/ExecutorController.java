package controller.paradigm.tasks;


import controller.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ExecutorController extends AbstractController {

    private ScheduledThreadPoolExecutor pool;

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
            if (new RESTWikiGraph().setLanguage(language)) {
                success.run();
            } else {
                failure.run();
            }
        });
    }

    @Override
    protected void computeAsync(WikiGraphNodeFactory nodeFactory, WikiGraphManager graph, int depth, String term,
                                String language, Consumer<WikiGraphManager> onComputeComplete, Runnable failure) {
        this.pool.execute(() -> new ComputeChildrenTask(nodeFactory, graph, depth, term) {
            @Override
            public void onCompletion(CountedCompleter<?> caller) {
                onComputeComplete.accept(graph);
            }

            @Override
            public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
                failure.run();
                return false;
            }
        }.compute());
    }

    @Override
    protected void schedule(int updateDelay, Runnable autoUpdate) {
        this.pool.schedule(autoUpdate, updateDelay, TimeUnit.MILLISECONDS);
    }
}
