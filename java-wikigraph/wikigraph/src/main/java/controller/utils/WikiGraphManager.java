package controller.utils;

import org.graphstream.graph.Graph;
import view.GraphDisplay;

import java.util.concurrent.locks.Lock;

public interface WikiGraphManager {

    /**
     * Returns a lock that grants mutual exclusion between threads that are computing the exact
     * same term at the same time. This is useful if you need to create nodes before edges, without
     * duplicate nodes, based on the terms and their children.
     *
     * @param nodeTerm the term that is paired with the lock
     * @return the lock paired with input term
     */
    Lock getLockOn(final String nodeTerm);

    /**
     * Sets the graph as aborted, in real time, atomically.
     */
    void setAborted();

    /**
     * Checks if this graph was aborted during computation, or maybe later.
     * @return true if the graph was flagged as aborted
     */
    boolean isAborted();

    /**
     * Checks if the graph contains a term.
     * @param term the term
     * @return true if it's present in the graph
     */
    boolean contains(final String term);

    /**
     * Adds a node to the graph.
     * @param term the term or ID of the node
     * @param depth the depth at which the node is found
     * @param language the node wikipedia language
     * @return true if the node was added and wasn't already present, false otherwise
     */
    boolean addNode(final String term, final int depth, final String language);

    /**
     * Adds a directed edge to the graph.
     * @param idFrom the id of the node From
     * @param idTo the id of the node To
     * @return true if edge wasn't already added and both node exists, so the edge is now in the graph;
     * false otherwise
     */
    boolean addEdge(final String idFrom, final String idTo);

    /**
     * Returns the graph-stream graph associated with this computation.
     * @return a {@link Graph}
     */
    Graph graph();

    /**
     * The root ID of the graph.
     * @return the root ID as {@link String}
     */
    String getRootID();

    /**
     * Sets the root ID for this graph.
     * @param term the ID of the root node
     */
    void setRootID(final String term);

    /**
     * Sets the output display for the graph. Updated only in real time
     * during graph population.
     * @param view a {@link GraphDisplay}
     */
    void setGraphDisplay(final GraphDisplay view);
}
