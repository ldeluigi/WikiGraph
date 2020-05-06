package controller.paradigm.reactivex;

import controller.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.paradigm.eventloop.EventLoopController;
import controller.paradigm.tasks.ComputeChildrenTask;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.*;
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
    public ReactiveXController(View view) { super(view); }

    @Override
    protected void checkLanguage(String language, Runnable success, Runnable failure) {
        Observable.just(language).subscribeOn(Schedulers.io()).subscribe(obs -> {
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
    protected void exit() { }

    @Override
    protected WikiGraphManager wikiGraphManager() {
        return SynchronizedWikiGraphManager.threadSafe();
    }

    @Override
    protected void computeAsync(WikiGraphNodeFactory nodeFactory, WikiGraphManager graph, int depth, String term, String language, Runnable onComputeComplete, Runnable failure) {
        //TODO da vedere
        Observable.just(language)
                .subscribeOn(Schedulers.io())
                .map(obs -> nodeFactory.setLanguage(language))
                .doOnError(obs -> failure.run())
                .subscribe(languageIsSetCorrectly -> {
                    if (languageIsSetCorrectly){
                        this.process();
                    }
                }, throwable -> failure.run(), () -> onComputeComplete.run());
    }

    private void process() {
    }

    @Override
    protected void schedule(int updateDelay, Runnable autoUpdate) {
        Completable.timer(updateDelay, TimeUnit.MICROSECONDS)
                .subscribe(() -> {
                    try {
                        autoUpdate.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
