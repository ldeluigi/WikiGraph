package model;

public interface NodeDifference {
    /**
     * Returns true if this difference means an addition.
     * @return true if newer graph had a node the older didn't
     */
    boolean isAdd();

    /**
     * Returns true if this difference means a replacement.
     * @return true if newer graph had a node the older had but with different properties
     */
    boolean isReplace();

    /**
     * Returns true if this difference means a removal.
     * @return true if newer graph didn't have a node the older did
     */
    boolean isRemove();

    /**
     * Returns the old version of the node.
     * @return the old node, or null if {@link #isAdd()}
     */
    WikiGraphNode oldNode();

    /**
     * Returns the new version of the node.
     * @return the new node, or null if {@link #isRemove()}
     */
    WikiGraphNode newNode();
}
