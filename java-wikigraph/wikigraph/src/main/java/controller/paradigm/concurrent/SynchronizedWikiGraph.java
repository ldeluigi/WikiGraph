package controller.paradigm.concurrent;

import controller.PartialWikiGraph;
import model.MutableWikiGraph;
import model.Pair;
import model.WikiGraphNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements: <br/>
 * - A monitor for a {@link MutableWikiGraph} <br/>
 * - A container for the state "aborted" (yes/no) to abrupt computation <br/>
 * - A manager for {@link Lock}s that are separate for each term, to grant mutual exclusion between
 *   tasks relative to the same term.
 */
public class SynchronizedWikiGraph implements ConcurrentWikiGraph {
    private final Map<String, Lock> locks = new HashMap<>();
    private final PartialWikiGraph graph;

    private SynchronizedWikiGraph(final PartialWikiGraph graph) {
        this.graph = graph;
    }

    private SynchronizedWikiGraph() {
        this(new PartialWikiGraphImpl());
    }

    /**
     * Returns an empty fresh new {@link SynchronizedWikiGraph}.
     * @return a new empty synchronized graph
     */
    public static SynchronizedWikiGraph empty() {
        return new SynchronizedWikiGraph();
    }


    @Override
    public Lock getLockOn(final String nodeTerm) {
        synchronized (this.locks) {
            if (this.locks.containsKey(nodeTerm)) {
                return this.locks.get(nodeTerm);
            }
            final Lock newLock = new ReentrantLock();
            this.locks.put(nodeTerm, newLock);
            return newLock;
        }
    }

    @Override
    public synchronized boolean add(final WikiGraphNode node) {
        return this.graph.add(node);
    }

    @Override
    public synchronized boolean remove(final String nodeTerm) {
        return this.graph.remove(nodeTerm);
    }

    @Override
    public synchronized boolean set(final WikiGraphNode node) {
        return this.graph.set(node);
    }

    @Override
    public synchronized Set<String> terms() {
        return this.graph.terms();
    }

    @Override
    public synchronized Collection<WikiGraphNode> nodes() {
        return this.graph.nodes();
    }

    @Override
    public synchronized Set<Pair<String, String>> termEdges() {
        return this.graph.termEdges();
    }

    @Override
    public synchronized boolean contains(final String term) {
        return this.graph.contains(term);
    }

    @Override
    public String getRoot() {
        return this.graph.getRoot();
    }

    @Override
    public void setAborted() {
        synchronized (this.graph) {
            this.graph.setAborted();
        }
    }

    @Override
    public boolean isAborted() {
        synchronized (this.graph) {
            return this.graph.isAborted();
        }
    }
}
