package controller.paradigm.eventloop;

import controller.Controller;
import controller.PartialWikiGraph;
import controller.PartialWikiGraphImpl;
import controller.api.RESTWikiGraph;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;
import view.ViewEvent.EventType;

public class EventLoopController implements Controller {

    private final View view;
    private final Vertx vertx;

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
                }
                event.onComplete();
            });
        }
    }

    private void startComputing(String term, int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        final PartialWikiGraph graph = new PartialWikiGraphImpl();
        new VertxNodeRecursion(this.vertx, nodeFactory, graph, this.view, depth, term).compute();
    }

    @Override
    public void start() {
        view.start();
    }
}
