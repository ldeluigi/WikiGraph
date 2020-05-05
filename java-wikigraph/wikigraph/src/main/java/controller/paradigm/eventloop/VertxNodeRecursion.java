package controller.paradigm.eventloop;

import controller.utils.NodeRecursion;
import controller.graphstream.GraphDisplaySink;
import controller.utils.WikiGraphManager;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.util.List;

public class VertxNodeRecursion extends NodeRecursion implements Handler<Void> {

    private final Vertx vertx;
    private final Runnable atCompletion;
    private int childrenYetToComplete = 0;

    protected VertxNodeRecursion(VertxNodeRecursion father, String term) {
        super(father, term);
        this.vertx = father.getVertx();
        this.atCompletion = father::childCompleted;
    }

    public VertxNodeRecursion(Vertx vertx, WikiGraphNodeFactory factory, WikiGraphManager graph, int maxDepth, String term, Runnable atCompletion) {
        super(factory, graph, maxDepth, term);
        this.vertx = vertx;
        this.atCompletion = atCompletion;
    }

    @Override
    public void compute() {
        this.vertx.executeBlocking(this::blockingCode, false, this::nodeHandler);
    }

    @Override
    protected void complete() {
        this.atCompletion.run();
    }

    @Override
    protected void childBirth(String term) {
        this.childrenYetToComplete++;
        new VertxNodeRecursion(this, term).compute();
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

    private void blockingCode(final Promise<WikiGraphNode> promise) {
        if (this.getGraph().isAborted()) {
            promise.fail("Graph aborted");
            return;
        }
        final WikiGraphNode result;
        if (this.getDepth() == 0) {
            if (this.getTerm() == null) { //random
                result = this.getNodeFactory().random(0);
            } else { //search
                WikiGraphNode temp = this.getNodeFactory().from(this.getTerm(), 0);
                if (temp == null) {
                    final List<Pair<String, String>> closest = this.getNodeFactory().search(this.getTerm());
                    if (closest.size() > 0) {
                        temp = this.getNodeFactory().from(closest.get(0).getKey(), 0);
                    }
                }
                result = temp;
            }
            if (result != null) {
                this.getGraph().setRootID(result.term());
            }
        } else {
            result = this.getNodeFactory().from(this.getTerm(), this.getDepth());
        }
        if (result != null) {
            promise.complete(result);
        } else {
            promise.fail(this.getTerm() + " not found.");
        }
    }

    private void nodeHandler(final AsyncResult<WikiGraphNode> result) {
        if (this.getGraph().isAborted()) {
            this.abort();
            return;
        }
        if (result.succeeded()) {
            final WikiGraphNode node = result.result();
            setID(node.term());
            if (getGraph().contains(node.term())) {
                getGraph().addEdge(getFatherID(), node.term());
            } else {
                getGraph().addNode(node.term(), getDepth(), getNodeFactory().getLanguage());
                if (getDepth() > 0) {
                    getGraph().addEdge(getFatherID(), node.term());
                }
                if (getDepth() < getMaxDepth()) {
                    for (String child : node.childrenTerms()) {
                        childBirth(child);
                    }
                }
            }
        }
        if (this.childrenYetToComplete == 0) {
            complete();
        }
    }
}
