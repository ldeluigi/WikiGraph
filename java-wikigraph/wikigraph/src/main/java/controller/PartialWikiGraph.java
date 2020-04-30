package controller;

import model.MutableWikiGraph;

public interface PartialWikiGraph extends MutableWikiGraph {
    /**
     * Sets this graph computation as aborted.
     * Every node computation should hang itself as soon as possible from now on.
     */
    void setAborted();

    /**
     * Checks whether this graph computation has been aborted or not.
     * @return true if computation has been aborted
     */
    boolean isAborted();
}
