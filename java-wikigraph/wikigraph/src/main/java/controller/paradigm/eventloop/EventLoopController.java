package controller.paradigm.eventloop;

import model.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.WikiGraphNodeFactory;
import view.View;

import java.io.IOException;

/**
 * Vertx implementation of an {@link AbstractController}.
 */
public class EventLoopController extends AbstractController {

    private Vertx vertx;

    /**
     * Creates a {@link EventLoopController} that uses the Vertx event loop and its worker pool.
     *
     * @param view the view where data is displayed
     */
    public EventLoopController(final View view) {
        super(view);
    }

    @Override
    public void start() {
        this.vertx = Vertx.vertx();
        super.start();
    }

    @Override
    protected void exit() {
        this.vertx.close();
    }

    @Override
    protected WikiGraphManager wikiGraphManager() {
        return SynchronizedWikiGraphManager.empty();
    }

    @Override
    protected void checkLanguage(final String language, final Runnable success, final Runnable failure) {
        checkLanguageFuture(new RESTWikiGraph(), language)
                .onSuccess(exists -> {
                    if (exists) {
                        success.run();
                    } else {
                        failure.run();
                    }
                })
                .onFailure(Throwable::printStackTrace);
    }

    private Future<Boolean> checkLanguageFuture(final WikiGraphNodeFactory nodeFactory, final String language) {
        final Promise<Boolean> pr = Promise.promise();
        this.vertx.<Boolean>executeBlocking(p -> {
                    try {
                        p.complete(nodeFactory.setLanguage(language));
                    } catch (IOException e) {
                        p.fail(e);
                    }
                },
                result -> {
                    if (result.succeeded()) {
                        pr.complete(result.result());
                    } else {
                        pr.fail(result.cause());
                    }
                });
        return pr.future();
    }

    @Override
    protected void computeAsync(final WikiGraphNodeFactory nodeFactory, final WikiGraphManager graph, final int depth,
                                final String term, final String language, final Runnable onComputeComplete,
                                final Runnable failure) {
        checkLanguageFuture(nodeFactory, language)
                .flatMap(Void -> {
                    final VertxNodeRecursion v = new VertxNodeRecursion(this.vertx,
                            nodeFactory,
                            graph,
                            depth,
                            term
                    );
                    v.compute();
                    return v;
                })
                .onSuccess(g -> onComputeComplete.run())
                .onFailure(cause -> failure.run());
    }

    @Override
    protected void schedule(final int updateDelay, final Runnable autoUpdate) {
        this.vertx.setTimer(Math.max(updateDelay, 1), p -> autoUpdate.run());
    }
}
