package controller.paradigm.reactivex;

import model.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ReactiveXController extends AbstractController {

    public static int MAX_CONCURRENCY() {
        return Runtime.getRuntime().availableProcessors() * 5;
    }

    private ExecutorService executor;

    /**
     * Creates a {@link ReactiveXController} that uses Observable, Single and Completable
     *
     * @param view the view that displays the graph
     */
    public ReactiveXController(final View view) {
        super(view);
    }

    @Override
    protected void checkLanguage(final String language, final Runnable success, final Runnable failure) {
        Observable.just(language).subscribeOn(Schedulers.io()).map(l -> new RESTWikiGraph().setLanguage(l))
        .subscribe(b -> {
            if (b != null) {
                if (b) {
                    success.run();
                } else {
                    failure.run();
                }
            }
        });
    }

    @Override
    public void start() {
        this.executor = Executors.newFixedThreadPool(MAX_CONCURRENCY());
        super.start();
    }

    @Override
    protected void exit() {
        this.executor.shutdown();
    }

    @Override
    protected WikiGraphManager wikiGraphManager() {
        return SynchronizedWikiGraphManager.threadSafe();
    }

    @Override
    protected void computeAsync(final WikiGraphNodeFactory nodeFactory, final WikiGraphManager graph,
                                final int depth, final String term, final String language,
                                final Runnable onComputeComplete, final Runnable failure) {
        new RecursiveGraphOperation(nodeFactory, depth, term, graph, this.executor)
                .singleOrError()
                .onErrorComplete(t -> {failure.run(); return true;})
                .doOnSuccess(g -> onComputeComplete.run())
                .subscribe();
    }

    @Override
    protected void schedule(final int updateDelay, final Runnable autoUpdate) {
        Completable.timer(updateDelay, TimeUnit.MILLISECONDS)
                .subscribe(() -> {
                    try {
                        autoUpdate.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
