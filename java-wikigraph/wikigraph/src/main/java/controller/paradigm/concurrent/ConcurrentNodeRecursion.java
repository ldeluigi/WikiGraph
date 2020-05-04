package controller.paradigm.concurrent;

import controller.NodeRecursion;
import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import view.GraphDisplay;
import view.View;

import java.util.List;
import java.util.concurrent.locks.Lock;

public abstract class ConcurrentNodeRecursion extends NodeRecursion {
    private final ConcurrentWikiGraph graph;

    protected ConcurrentNodeRecursion(final ConcurrentNodeRecursion father, final String term) {
        super(father, term);
        this.graph = father.getConcurrentGraph();
    }

    protected ConcurrentNodeRecursion(final WikiGraphNodeFactory factory,
                                      final ConcurrentWikiGraph graph,
                                      final GraphDisplay view,
                                      final int maxDepth,
                                      final String term) {
        super(factory, graph, view, maxDepth, term);
        this.graph = graph;
    }

    private ConcurrentWikiGraph getConcurrentGraph() {
        return this.graph;
    }

    @Override
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
            final Lock lock = this.graph.getLockOn(result.term());
            lock.lock();
            try {
                if (this.graph.contains(result.term())) {
                    this.graph.addEdge(this.getFatherID(), result.term());
                    this.getView().addEdge(this.getFatherID(), result.term());
                } else {
                    this.getView().addNode(result.term(), this.getDepth(), this.getNodeFactory().getLanguage());
                    this.graph.addNode(result.term());
                    if (this.getDepth() > 0) {
                        this.graph.addEdge(this.getFatherID(), result.term());
                        this.getView().addEdge(this.getFatherID(), result.term());
                    }
                    if (this.getDepth() < this.getMaxDepth()) {
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
}
