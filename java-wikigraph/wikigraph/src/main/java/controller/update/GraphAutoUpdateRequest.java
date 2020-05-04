package controller.update;

import controller.utils.WikiGraphManager;
import model.WikiGraphNodeFactory;

public class GraphAutoUpdateRequest {

    private final WikiGraphNodeFactory nodeFactory;
    private final int depth;
    private WikiGraphManager graph;

    public GraphAutoUpdateRequest(WikiGraphNodeFactory nodeFactory, int depth, WikiGraphManager original) {
        this.nodeFactory = nodeFactory;
        this.depth = depth;
        this.graph = original;
    }

    public int getDepth() {
        return depth;
    }

    public WikiGraphNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public WikiGraphManager getOriginal() {
        return this.graph;
    }

    public void updateOriginal(final WikiGraphManager graph) {
        this.graph = graph;
    }
}

