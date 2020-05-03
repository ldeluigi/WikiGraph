package controller.update;

import controller.paradigm.concurrent.ConcurrentWikiGraph;
import model.WikiGraphNodeFactory;
import view.GraphDisplay;

public class GraphAutoUpdateRequest {

    private final WikiGraphNodeFactory nodeFactory;
    private final int depth;
    private final GraphDisplay view = new NoOpView();
    private ConcurrentWikiGraph graph;

    public GraphAutoUpdateRequest(WikiGraphNodeFactory nodeFactory, int depth, ConcurrentWikiGraph original) {
        this.nodeFactory = nodeFactory;
        this.depth = depth;
        this.graph = original;
    }

    public GraphDisplay getView() {
        return view;
    }

    public int getDepth() {
        return depth;
    }

    public WikiGraphNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public ConcurrentWikiGraph getOriginal() {
        return this.graph;
    }

    public void updateOriginal(final ConcurrentWikiGraph graph) {
        this.graph = graph;
    }
}

