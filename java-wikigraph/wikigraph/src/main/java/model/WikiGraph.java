package model;

import java.util.Collection;
import java.util.Set;

/**
 * Interface for an immutable graph of {@link WikiGraphNode},
 * with query methods.
 */
public interface WikiGraph {

    /**
     * Returns every term present in the graph.
     *
     * @return a set of terms
     */
    Set<String> terms();

    /**
     * Returns the list of structural nodes computed for the graph (non-leaf nodes).
     *
     * @return a collection of {@link WikiGraphNode}
     */
    Collection<WikiGraphNode> nodes();

    /**
     * Returns every edge present in the graph as a set of {@link Pair} of {@link String}.
     * Order in the pair is important: the edge goes from left to right.
     *
     * @return a set of edges
     */
    Set<Pair<String, String>> termEdges();

    /**
     * Checks if term is present in the graph as a node id.
     * @param term the term to check
     * @return true if term is a node id for a node
     */
    boolean contains(final String term);
}
