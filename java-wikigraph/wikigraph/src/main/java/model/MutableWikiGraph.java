package model;

public interface MutableWikiGraph extends WikiGraph {
    /**
     * Adds a node to the graph, using its children for edges.
     * If a node with the same term was already present false is returned.
     * @param node the node
     * @return true if node was added
     */
    boolean add(final WikiGraphNode node);

    /**
     * Removes the node with the specified term, if present.
     * @param nodeTerm the term
     * @return true if node was present and thus removed
     */
    boolean remove(final String nodeTerm);

    /**
     * Replaces the node in the graph, using the {@link WikiGraphNode#term()} as id.
     * @param node the new node
     * @return true if node was present and thus replaced
     */
    boolean set(final WikiGraphNode node);
}
