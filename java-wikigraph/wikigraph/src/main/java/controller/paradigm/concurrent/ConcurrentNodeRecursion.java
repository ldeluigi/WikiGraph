package controller.paradigm.concurrent;

import controller.NodeRecursion;
import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
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
                                      final View view,
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
            this.setID(result.term());
            final Lock lock = this.graph.getLockOn(result.term());
            lock.lock();
            try {
                if (this.graph.contains(result.term())) {
                    this.getView().addEdge(this.getFatherID(), result.term());
                } else {
                    this.getView().addNode(result.term(), this.getDepth(), this.getNodeFactory().getLanguage());
                    this.graph.add(result);
                    if (this.getDepth() > 0) {
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
