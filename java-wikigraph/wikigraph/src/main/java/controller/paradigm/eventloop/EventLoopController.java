package controller.paradigm.eventloop;

import controller.Controller;
import controller.PartialWikiGraph;
import controller.PartialWikiGraphImpl;
import controller.api.RESTWikiGraph;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;
import view.ViewEvent.EventType;

import java.util.Locale;

public class EventLoopController implements Controller {

    private final View view;
    private final Vertx vertx;
    private String language = Locale.ENGLISH.getLanguage();

    public EventLoopController(View view) {
        this.view = view;
        view.addEventListener(this);
        this.vertx = Vertx.vertx();
    }

    @Override
    public void notifyEvent(final ViewEvent event) {
        if (event.getType().equals(EventType.EXIT)) {
            this.vertx.close();
        } else {
            this.vertx.runOnContext(Void -> {
                switch (event.getType()) {
                    case CLEAR:
                        //this.view.clearGraph();
                        break;
                    case SEARCH:
                        startComputing(event.getText(), event.getDepth());
                        break;
                    case RANDOM_SEARCH:
                        startComputing(null, event.getDepth());
                        break;
                    case LANGUAGE:
                        this.vertx.executeBlocking((Handler<Promise<String>>) p -> {
                            if (new RESTWikiGraph().setLanguage(event.getText())) {
                                p.complete(event.getText());
                            } else {
                                p.fail("Language doesn't exist");
                            }
                        }, result -> {
                            if (result.succeeded()) {
                                language = result.result();
                                event.onComplete(true);
                            } else {
                                event.onComplete(false);
                            }
                        });
                }
            });
        }
    }

    private void startComputing(String term, int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        final PartialWikiGraph graph = new PartialWikiGraphImpl();
        this.vertx.executeBlocking(p -> {
                    if (nodeFactory.setLanguage(this.language)) {
                        p.complete();
                    } else {
                        p.fail("Language doesn't exist, aborting");
                    }
                },
                result -> {
                    if (result.succeeded()) {
                        new VertxNodeRecursion(this.vertx, nodeFactory, graph, this.view, depth, term).compute();
                    } else {
                        System.err.println(result.cause());
                    }
                });
    }

    @Override
    public void start() {
        view.start();
    }
}
