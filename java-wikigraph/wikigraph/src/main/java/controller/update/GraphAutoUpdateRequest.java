package controller.update;

import controller.utils.WikiGraphManager;
import model.WikiGraphNodeFactory;

/**
 * Represents a request for auto-updating.
 */
public class GraphAutoUpdateRequest {

    private final WikiGraphNodeFactory nodeFactory;
    private final int depth;
    private WikiGraphManager graph;

    /**
     * Creates a request for auto-updating with the given parameters.
     * @param nodeFactory the factory to use
     * @param depth the max depth of the graph
     * @param original the original graph for comparison
     */
    public GraphAutoUpdateRequest(WikiGraphNodeFactory nodeFactory, int depth, WikiGraphManager original) {
        this.nodeFactory = nodeFactory;
        this.depth = depth;
        this.graph = original;
    }

    /**
     * Returns the depth of the graph that requested an update.
     * @return the depth as integer
     */
    public int getDepth() {
        return this.depth;
    }

    /**
     * Returns the node factory for the update.
     * @return the {@link WikiGraphNodeFactory}
     */
    public WikiGraphNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    /**
     * Returns the original graph to be compared with the updated version, and maybe substituted.
     * @return the original copy of the graph
     */
    public WikiGraphManager getOriginal() {
        return this.graph;
    }

    /**
     * Overwrites the original copy with a new one.
     * @param graph the new version of the graph
     */
    public void updateOriginal(final WikiGraphManager graph) {
        this.graph = graph;
    }
}

