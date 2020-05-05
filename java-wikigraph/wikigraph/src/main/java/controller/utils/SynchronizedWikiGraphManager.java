package controller.utils;

import controller.graphstream.GraphDisplaySink;
import controller.update.NoOpView;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;
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
public class SynchronizedWikiGraphManager implements WikiGraphManager {
    private final Map<String, Lock> locks = new HashMap<>();
    private final AtomicBoolean aborted = new AtomicBoolean(false);
    private final Graph graph;
    private String root;
    private GraphDisplay view = new NoOpView();

    private SynchronizedWikiGraphManager(final boolean mutexOnGraph) {
        final Graph g = new MultiGraph("WikiGraphModel");
        if (mutexOnGraph) {
            this.graph = Graphs.synchronizedGraph(g);
        } else {
            this.graph = g;
        }
        this.graph.addAttribute("sync", mutexOnGraph);
    }

    /**
     * Returns an empty fresh new {@link SynchronizedWikiGraphManager},
     * with a mutex protection on the internal graph.
     *
     * @return a new empty synchronized graph
     */
    public static SynchronizedWikiGraphManager threadSafe() {
        return new SynchronizedWikiGraphManager(true);
    }

    /**
     * Returns an empty fresh new {@link SynchronizedWikiGraphManager},
     * without a mutex protecting the graph.
     *
     * @return a new empty synchronized graph
     */
    public static SynchronizedWikiGraphManager empty() {
        return new SynchronizedWikiGraphManager(false);
    }

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
            final Node n = this.graph.addNode(term);
            if (n != null) {
                n.addAttribute(GraphDisplaySink.DEPTH_ATTRIBUTE, depth);
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
            final Edge edge = this.graph.addEdge(idFrom + "@@@" + idTo, from, to, true);
            if (edge != null) {
                edge.addAttribute(" ", ""); // bug workaround
                this.view.addEdge(idFrom, idTo);
                return true;
            }
        }
        return false;
    }

    public Graph graph() {
        return this.graph;
    }

    @Override
    public String getRootID() {
        return this.root;
    }

    @Override
    public void setRootID(String term) {
        this.root = term;
    }

    @Override
    public void setGraphDisplay(final GraphDisplay view) {
        this.view = view;
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
