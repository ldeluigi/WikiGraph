package controller.paradigm.reactivex;

import controller.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import model.WikiGraphNodeFactory;
import view.View;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ReactiveXController extends AbstractController {

    /**
     * Creates a {@link ReactiveXController} that uses //TODO
     *
     * @param view the view that displays the graph
     */
    public ReactiveXController(View view) {
        super(view);
    }

    @Override
    protected void checkLanguage(String language, Runnable success, Runnable failure) {
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
    protected void exit() {
    }

    @Override
    protected WikiGraphManager wikiGraphManager() {
        return SynchronizedWikiGraphManager.threadSafe();
    }

    @Override
    protected void computeAsync(WikiGraphNodeFactory nodeFactory, WikiGraphManager graph,
                                int depth, String term, String language,
                                Runnable onComputeComplete, Runnable failure) {
        new RecursiveGraphOperation(nodeFactory, depth, term, graph)
                .singleOrError()
                .onErrorComplete(t -> {failure.run(); return true;})
                .doOnSuccess(g -> onComputeComplete.run())
                .subscribe();
    }

    @Override
    protected void schedule(int updateDelay, Runnable autoUpdate) {
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
