package controller.utils;

import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.util.List;
import java.util.Optional;

public abstract class NodeRecursion {

    private final WikiGraphNodeFactory factory;
    private final WikiGraphManager graph;
    private final int maxDepth;
    private final String term;
    private final String fatherID;
    private final int depth;
    private Optional<String> id = Optional.empty();

    protected NodeRecursion(final NodeRecursion father, final String term) {
        this.factory = father.getNodeFactory();
        this.graph = father.getGraph();
        this.maxDepth = father.getMaxDepth();
        this.term = term;
        if (father.getID().isEmpty()) {
            throw new IllegalArgumentException("Father has not yet computed");
        }
        this.fatherID = father.getID().get();
        this.depth = father.getDepth() + 1;
    }


    protected NodeRecursion(final WikiGraphNodeFactory factory,
                            final WikiGraphManager graph,
                            final int maxDepth,
                            final String term) {
        this.factory = factory;
        this.graph = graph;
        this.maxDepth = maxDepth;
        this.term = term;
        this.fatherID = null;
        this.depth = 0;
    }

    public void compute() {
        if (this.graph.isAborted()) {
            abort();
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
            this.setID(result.term());
            if (this.getGraph().contains(result.term())) {
                this.getGraph().addEdge(this.getFatherID(), result.term());
            } else {
                this.getGraph().addNode(result.term(), depth, this.getNodeFactory().getLanguage());
                if (this.getDepth() > 0) {
                    this.getGraph().addEdge(this.getFatherID(), result.term());
                }
                if (this.getDepth() < this.getMaxDepth()) {
                    for (String child : result.childrenTerms()) {
                        childBirth(child);
                    }
                }
            }
        }
        complete();
    }

    protected abstract void complete();

    protected abstract void childBirth(final String term);

    public abstract void abort();

    protected final int getDepth() {
        return this.depth;
    }

    protected final String getTerm() {
        return this.term;
    }

    protected final WikiGraphNodeFactory getNodeFactory() {
        return this.factory;
    }

    protected final int getMaxDepth() {
        return this.maxDepth;
    }

    protected final Optional<String> getID() {
        return this.id;
    }

    protected final void setID(final String id) {
        this.id = Optional.of(id);
    }

    protected final String getFatherID() {
        return this.fatherID;
    }

    protected final WikiGraphManager getGraph() {
        return this.graph;
    }
}
