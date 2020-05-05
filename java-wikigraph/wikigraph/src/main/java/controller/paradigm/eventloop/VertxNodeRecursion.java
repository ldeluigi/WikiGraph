package controller.paradigm.eventloop;

import controller.utils.AbortedOperationException;
import controller.utils.NodeRecursion;
import controller.utils.WikiGraphManager;
import io.vertx.core.*;
import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.util.List;

/**
 * Recursive graph computation implemented with Vertx.
 */
public class VertxNodeRecursion extends NodeRecursion implements Future<WikiGraphManager> {

    private final Vertx vertx;
    private final Promise<WikiGraphManager> promise = Promise.promise();
    private int childrenYetToComplete = 0;

    protected VertxNodeRecursion(VertxNodeRecursion father, String term) {
        super(father, term);
        this.vertx = father.getVertx();
    }

    /**
     * Creates a {@link VertxNodeRecursion} with given parameters.
     *
     * @param vertx    the vertx instance that manages the graph
     * @param factory  see {@link NodeRecursion}
     * @param graph    see {@link NodeRecursion}
     * @param maxDepth see {@link NodeRecursion}
     * @param term     see {@link NodeRecursion}
     */
    public VertxNodeRecursion(Vertx vertx, WikiGraphNodeFactory factory, WikiGraphManager graph, int maxDepth, String term) {
        super(factory, graph, maxDepth, term);
        this.vertx = vertx;
    }

    @Override
    public void compute() {
        this.vertx.executeBlocking(this::blockingCode, false, this::nodeHandler);
    }

    @Override
    protected void complete() {
        this.promise.complete(this.getGraph());
    }

    @Override
    protected void childBirth(String term) {
        this.childrenYetToComplete++;
        final VertxNodeRecursion v = new VertxNodeRecursion(this, term);
        v.onSuccess(g -> this.childCompleted())
                .onFailure(t -> this.abort());
        v.compute();
    }

    @Override
    public void abort() {
        this.promise.tryFail(new AbortedOperationException());
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
        if (result.failed() || this.getGraph().isAborted()) {
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

    @Override
    public boolean isComplete() {
        return this.promise.future().isComplete();
    }

    @Override
    public Future<WikiGraphManager> onComplete(Handler<AsyncResult<WikiGraphManager>> handler) {
        return this.promise.future().onComplete(handler);
    }

    @Override
    public Handler<AsyncResult<WikiGraphManager>> getHandler() {
        return this.promise.future().getHandler();
    }

    @Override
    public WikiGraphManager result() {
        return this.promise.future().result();
    }

    @Override
    public Throwable cause() {
        return this.promise.future().cause();
    }

    @Override
    public boolean succeeded() {
        return this.promise.future().succeeded();
    }

    @Override
    public boolean failed() {
        return this.promise.future().failed();
    }
}
