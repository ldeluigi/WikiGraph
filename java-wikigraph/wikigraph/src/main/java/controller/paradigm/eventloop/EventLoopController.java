package controller.paradigm.eventloop;

import controller.api.RESTWikiGraph;
import controller.paradigm.AbstractController;
import controller.utils.SynchronizedWikiGraphManager;
import controller.utils.WikiGraphManager;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.function.Consumer;

public class EventLoopController extends AbstractController {

    private Vertx vertx;

    public EventLoopController(View view) {
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
    protected void checkLanguage(String language, Runnable success, Runnable failure) {
        checkLanguageFuture(new RESTWikiGraph(), language)
                .onSuccess(Void -> success.run())
                .onFailure(Void -> failure.run());
    }

    private Future<Void> checkLanguageFuture(final WikiGraphNodeFactory nodeFactory, final String language) {
        final Promise<Void> pr = Promise.promise();
        this.vertx.executeBlocking(p -> {
                    if (nodeFactory.setLanguage(language)) {
                        p.complete();
                    } else {
                        p.fail("Language doesn't exist, aborting");
                    }
                },
                result -> {
                    if (result.succeeded()) {
                        pr.complete(null);
                    } else {
                        pr.fail(result.cause());
                    }
                });
        return pr.future();
    }

    @Override
    protected void computeAsync(WikiGraphNodeFactory nodeFactory, WikiGraphManager graph, int depth, String term,
                                String language, Consumer<WikiGraphManager> onComputeComplete, Runnable failure) {
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
                .onSuccess(onComputeComplete::accept)
                .onFailure(cause -> failure.run());
    }

    @Override
    protected void schedule(int updateDelay, Runnable autoUpdate) {
        this.vertx.setTimer(updateDelay, p -> autoUpdate.run());
    }
}
