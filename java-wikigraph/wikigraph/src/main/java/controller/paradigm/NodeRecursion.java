package controller.paradigm;

import controller.ConcurrentWikiGraph;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.Optional;
import java.util.concurrent.locks.Lock;

public abstract class NodeRecursion {
    
    private final WikiGraphNodeFactory factory;
    private final ConcurrentWikiGraph graph;
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
                         final ConcurrentWikiGraph graph,
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
        if (this.depth == 0) {
            result = initRoot();
        } else {
            result = this.factory.from(this.term);
        }
        if (result != null) {
            this.id = Optional.of(result.term());
            final Lock lock = this.graph.getLockOn(this.id.get());
            lock.lock();
            try {
                if (this.graph.contains(this.id.get())) {
                    this.view.addEdge(this.fatherID, this.id.get());
                } else {
                    this.view.addNode(this.id.get(), this.depth, this.factory.getLanguage());
                    this.graph.add(result);
                    if (this.depth > 0) {
                        view.addEdge(this.fatherID, this.id.get());
                    }
                    if (this.depth < this.maxDepth) {
                        for (String child : result.childrenTerms()) {
                            childBirth(child);
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        complete();
    }

    protected abstract void complete();

    protected abstract void childBirth(final String term);

    protected WikiGraphNode initRoot() {
        if (this.term == null) { //random
            return this.factory.random();
        } else { //search
            return this.factory.from(this.term);
        }
    }

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
    
    public ConcurrentWikiGraph getGraph() {
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
}
