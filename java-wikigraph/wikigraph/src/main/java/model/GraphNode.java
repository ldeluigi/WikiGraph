package model;

import java.util.Set;

public interface GraphNode {
    /**
     * Returns the list of terms that are linked from this node Wiki page.
     * @return every term as Set of String
     */
    Set<String> childrenTerms();

    /**
     * Returns the term to which this node refers to.
     * @return the term as a string
     */
    String term();

    /**
     * Should return true only if o is a {@link GraphNode} of the same term.
     * @param other the other node
     * @return true if it's the same node
     */
    boolean equals(GraphNode other);
}
