package controller.paradigm.concurrent;

import controller.update.NoOpView;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import view.GraphDisplay;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements: <br/>
 * - A container for the state "aborted" (yes/no) to abrupt computation <br/>
 * - A manager for {@link Lock}s that are separate for each term, to grant mutual exclusion between
 * tasks relative to the same term.
 */
public class SynchronizedWikiGraph implements ConcurrentWikiGraph {
    private final Map<String, Lock> locks = new HashMap<>();
    private final AtomicBoolean aborted = new AtomicBoolean(false);
    private String root;
    private final Graph graph = new MultiGraph("WikiGraph2");
    private GraphDisplay view = new NoOpView();

    @Override
    public void setAborted() {
        this.aborted.set(true);
    }

    @Override
    public boolean isAborted() {
        return this.aborted.get();
    }

    @Override
    public boolean contains(final String term) {// TODO check che si comporti come deve
        return this.graph.getNode(term) != null;
    }

    @Override
    public boolean addNode(final String term, final int depth, final String language) {
        if (!this.contains(term)) {
            if (this.graph.addNode(term) != null) {
                this.view.addNode(term, depth, language);
                return true;
            }
        } else {
            System.err.println("INFO: DUPLICATE NODE IGNORED - " + term);
        }
        return false;
    }

    @Override
    public boolean addEdge(final String idFrom, final String idTo) {
        final Node from = this.graph.getNode(idFrom);
        if (from == null) {
            System.err.println("ERROR: (Synchr) node " + idFrom + " not found. Aborting edge " + idFrom + "@@@" + idTo);
            return false;
        }
        final Node to = this.graph.getNode(idTo);
        if (to == null) {
            System.err.println("ERROR: (Synchr) node " + idTo + " not found. Aborting edge " + idFrom + "@@@" + idTo);
            return false;
        }
        final String name = idFrom + "@@@" + idTo;
        if (this.graph.getEdge(name) == null) {
            if (this.graph.addEdge(idFrom + "@@@" + idTo, from, to, true) != null) {
                this.view.addEdge(idFrom, idTo);
                return true;
            }
        }
        return false;
    }

    public Graph getGraph() {
        return this.graph;
    }

    @Override
    public void setRootID(String term) {
        this.root = term;
    }

    @Override
    public String getRootID() {
        return this.root;
    }

    @Override
    public void setGraphDisplay(final GraphDisplay view) {
        this.view = view;
    }

    /**
     * Returns an empty fresh new {@link SynchronizedWikiGraph}.
     *
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
}
