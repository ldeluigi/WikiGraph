package controller.utils;

import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * A thread safe implementation for a {@link NodeRecursion}.
 * Note: thread safeness on the graph itself ({@link WikiGraphManager#graph()}
 * is not guaranteed and thus must be enforced externally.
 */
public abstract class ConcurrentNodeRecursion extends NodeRecursion {
    private final WikiGraphManager graph;

    protected ConcurrentNodeRecursion(final ConcurrentNodeRecursion father, final String term) {
        super(father, term);
        this.graph = father.getConcurrentGraph();
    }

    protected ConcurrentNodeRecursion(final WikiGraphNodeFactory factory,
                                      final WikiGraphManager graph,
                                      final int maxDepth,
                                      final String term) {
        super(factory, graph, maxDepth, term);
        this.graph = graph;
    }

    private WikiGraphManager getConcurrentGraph() {
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
                WikiGraphNode temp;
                try {
                    temp = this.getNodeFactory().from(new URL(this.getTerm()),this.getDepth());
                } catch (MalformedURLException | IllegalArgumentException e) {
                    temp = this.getNodeFactory().from(this.getTerm(), 0);
                    if (temp == null) {
                        final List<Pair<String, String>> closest = this.getNodeFactory().search(this.getTerm());
                        if (closest.size() > 0) {
                            temp = this.getNodeFactory().from(closest.get(0).getKey(), 0);
                        }
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
                    if ((int)this.graph.graph().getNode(result.term()).getAttribute("depth") > this.getDepth()){
                        this.graph.updateDepthNode(result.term(), this.getDepth());
                    }
                    this.graph.addEdge(this.getFatherID(), result.term());
                } else {
                    this.graph.addNode(result.term(), this.getDepth(), this.getNodeFactory().getLanguage());
                    if (this.getDepth() > 0) {
                        this.graph.addEdge(this.getFatherID(), result.term());
                    }
                    if (this.getDepth() < this.getMaxDepth()) {
                        for (final String child : result.childrenTerms()) {
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
