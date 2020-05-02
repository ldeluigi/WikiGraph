package controller.paradigm.eventloop;

import controller.NodeRecursion;
import controller.PartialWikiGraph;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.List;

public class VertxNodeRecursion extends NodeRecursion implements Handler<Void> {

    private final Vertx vertx;
    private int childrenYetToComplete = 0;
    private final Runnable atCompletion;

    protected VertxNodeRecursion(VertxNodeRecursion father, String term) {
        super(father, term);
        this.vertx = father.getVertx();
        this.atCompletion = () -> father.childCompleted();
    }

    public VertxNodeRecursion(Vertx vertx, WikiGraphNodeFactory factory, PartialWikiGraph graph, View view, int maxDepth, String term, Runnable atCompletion) {
        super(factory, graph, view, maxDepth, term);
        this.vertx = vertx;
        this.atCompletion = atCompletion;
    }

    @Override
    public void compute() {
        if (this.getGraph().isAborted()) {
            this.abort();
            return;
        }
        this.vertx.executeBlocking((Handler<Promise<WikiGraphNode>>) promise -> {
            if (this.getGraph().isAborted()) {
                promise.fail("Graph aborted");
                return;
            }
            final WikiGraphNode result;
            if (this.getDepth() == 0) {
                if (this.getTerm() == null) { //random
                    result = this.getNodeFactory().random();
                } else { //search
                    WikiGraphNode temp = this.getNodeFactory().from(this.getTerm());
                    if (temp == null) {
                        final List<Pair<String, String>> closest = this.getNodeFactory().search(this.getTerm());
                        if (closest.size() > 0) {
                            temp = this.getNodeFactory().from(closest.get(0).getKey());
                        }
                    }
                    result = temp;
                }
            } else {
                result = this.getNodeFactory().from(this.getTerm());
            }
            if (result != null) {
                promise.complete(result);
            } else {
                promise.fail(this.getTerm() + " not found.");
            }
        }, false, event -> {
            if (this.getGraph().isAborted()) {
                this.abort();
                return;
            }
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
            if (this.childrenYetToComplete == 0) {
                complete();
            }
        });
    }

    @Override
    protected void complete() {
        this.atCompletion.run();
    }

    @Override
    protected void childBirth(String term) {
        this.childrenYetToComplete++;
        this.vertx.runOnContext(new VertxNodeRecursion(this, term));
    }

    @Override
    public void abort() {
        this.atCompletion.run();
    }

    private Vertx getVertx() {
        return this.vertx;
    }

    private void childCompleted() {
        this.childrenYetToComplete--;
        if (this.childrenYetToComplete == 0) {
            complete();
        }
    }

    @Override
    public void handle(Void event) {
        this.compute();
    }
}
