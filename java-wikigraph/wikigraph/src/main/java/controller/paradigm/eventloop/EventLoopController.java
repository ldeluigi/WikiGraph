package controller.paradigm.eventloop;

import controller.Controller;
import controller.paradigm.NodeRecursion;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
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
                        this.view.clearGraph();
                        event.onComplete();
                        break;
                    case SEARCH:
                        final String term = event.getText();
                        final int depth = event.getDepth();
                        vertx.executeBlocking(this::computeTree);
                        break;
                }
            });
        }
    }

    private void computeTree(Promise<Object> promise) {
        //new NodeRecursion(promise, vertx)
        //nr.compute();
    }

    @Override
    public void start() {
        view.start();
    }
}
