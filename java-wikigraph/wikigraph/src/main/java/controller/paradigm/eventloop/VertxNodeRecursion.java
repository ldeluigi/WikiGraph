package controller.paradigm.eventloop;

import controller.NodeRecursion;
import controller.PartialWikiGraph;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import view.View;

public class VertxNodeRecursion extends NodeRecursion implements Handler<Void> {

    private final Vertx vertx;

    protected VertxNodeRecursion(VertxNodeRecursion father, String term) {
        super(father, term);
        this.vertx = father.getVertx();
    }

    public VertxNodeRecursion(Vertx vertx, WikiGraphNodeFactory factory, PartialWikiGraph graph, View view, int maxDepth, String term) {
        super(factory, graph, view, maxDepth, term);
        this.vertx = vertx;
    }

    @Override
    public void compute() {
        if (this.getGraph().isAborted()) {
            abort();
            return;
        }
        this.vertx.executeBlocking((Handler<Promise<WikiGraphNode>>) promise -> {
            final WikiGraphNode result;
            if (VertxNodeRecursion.this.getDepth() == 0) {
                if (VertxNodeRecursion.this.getTerm() == null) { //random
                    result = VertxNodeRecursion.this.getNodeFactory().random();
                } else { //search
                    result = VertxNodeRecursion.this.getNodeFactory().from(VertxNodeRecursion.this.getTerm());
                }
            } else {
                result = VertxNodeRecursion.this.getNodeFactory().from(VertxNodeRecursion.this.getTerm());
            }
            if (result != null) {
                promise.complete(result);
            } else {
                promise.fail(VertxNodeRecursion.this.getTerm() + " not found.");
            }
        }, event -> {
            if (event.succeeded()) {
                final WikiGraphNode result = event.result();
                setID(result.term());
                if (getGraph().contains(result.term())) {
                    getView().addEdge(getFatherID(), result.term());
                } else {
                    getView().addNode(result.term(), getDepth(), getNodeFactory().getLanguage());
                    getGraph().add(result);
                    if (getDepth() > 0) {
                        getView().addEdge(getFatherID(), result.term());
                    }
                    if (getDepth() < getMaxDepth()) {
                        for (String child : result.childrenTerms()) {
                            childBirth(child);
                        }
                    }
                }
            }
            complete();
        });
    }

    @Override
    protected void complete() {
    }

    @Override
    protected void childBirth(String term) {
        this.vertx.runOnContext(new VertxNodeRecursion(this, term));
    }

    @Override
    public void abort() {
    }

    private Vertx getVertx() {
        return this.vertx;
    }

    @Override
    public void handle(Void event) {
        this.compute();
    }
}
