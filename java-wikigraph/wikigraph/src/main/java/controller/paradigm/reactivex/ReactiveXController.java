package controller.paradigm.reactivex;

import controller.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.paradigm.eventloop.EventLoopController;
import controller.paradigm.tasks.ComputeChildrenTask;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
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
        //TODO controllare se è sullo stesso thread
        Observable.fromSingle(obs -> {
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
        Observable.fromSingle(observer -> {
            try {
                nodeFactory.setLanguage(language);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void schedule(int updateDelay, Runnable autoUpdate) {
        //TODO controllare se è sullo stesso thread
        Completable.timer(updateDelay, TimeUnit.SECONDS)
                .doOnComplete(() -> {
                    try {
                        autoUpdate.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }
}
