package controller.paradigm.eventloop;

import controller.Controller;
import controller.api.RESTWikiGraph;
import controller.paradigm.NodeRecursion;
import controller.paradigm.NodeRecursionBrutto;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.*;
import view.View;
import view.ViewEvent;
import view.ViewEvent.EventType;

import java.util.Collection;
import java.util.Set;

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
                        this.view.clearGraph();
                        event.onComplete();
                        break;
                    case SEARCH:
                        startComputing( event.getText(),event.getDepth());
                        break;
                }
            });
        }
    }

    private void startComputing(String term,int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        final MutableWikiGraph graph = new MutableGraphImpl();
        new NodeRecursionBrutto(nodeFactory, graph,this.view,depth,term, this.vertx).compute();
    }

    @Override
    public void start() {
        view.start();
    }
}
