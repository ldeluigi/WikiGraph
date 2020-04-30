package controller;

import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.Optional;

public abstract class NodeRecursion {

    private final WikiGraphNodeFactory factory;
    private final PartialWikiGraph graph;
    private final View view;
    private final int maxDepth;
    private final String term;
    private final String fatherID;
    private final int depth;
    private Optional<String> id = Optional.empty();

    protected NodeRecursion(final NodeRecursion father, final String term) {
        this.factory = father.getNodeFactory();
        this.graph = father.getGraph();
        this.view = father.getView();
        this.maxDepth = father.getMaxDepth();
        this.term = term;
        if (father.getID().isEmpty()) {
            throw new IllegalArgumentException("Father has not yet computed");
        }
        this.fatherID = father.getID().get();
        this.depth = father.getDepth() + 1;
    }

    protected NodeRecursion(final WikiGraphNodeFactory factory,
                            final PartialWikiGraph graph,
                            final View view,
                            final int maxDepth,
                            final String term) {
        this.factory = factory;
        this.graph = graph;
        this.view = view;
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
                result = this.getNodeFactory().random();
            } else { //search
                result = this.getNodeFactory().from(this.getTerm());
            }
        } else {
            result = this.getNodeFactory().from(this.getTerm());
        }
        if (result != null) {
            this.setID(result.term());
            if (this.getGraph().contains(result.term())) {
                this.getView().addEdge(this.getFatherID(), result.term());
            } else {
                this.getView().addNode(result.term(), this.getDepth(), this.getNodeFactory().getLanguage());
                this.getGraph().add(result);
                if (this.getDepth() > 0) {
                    this.getView().addEdge(this.getFatherID(), result.term());
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

    public int getDepth() {
        return this.depth;
    }

    public String getTerm() {
        return this.term;
    }

    public WikiGraphNodeFactory getNodeFactory() {
        return this.factory;
    }

    public PartialWikiGraph getGraph() {
        return this.graph;
    }

    public View getView() {
        return this.view;
    }

    public int getMaxDepth() {
        return this.maxDepth;
    }

    public Optional<String> getID() {
        return this.id;
    }

    protected final void setID(final String id) {
        this.id = Optional.of(id);
    }

    protected final String getFatherID() {
        return this.fatherID;
    }
}
