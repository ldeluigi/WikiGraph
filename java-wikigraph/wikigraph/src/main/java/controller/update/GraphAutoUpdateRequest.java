package controller.update;

import model.WikiGraph;
import model.WikiGraphNodeFactory;
import view.GraphDisplay;

public class GraphAutoUpdateRequest {

    private final WikiGraphNodeFactory nodeFactory;
    private final int depth;
    private final String root;
    private final GraphDisplay view = new NoOpView();
    private WikiGraph graph;

    public GraphAutoUpdateRequest(WikiGraph graph, WikiGraphNodeFactory nodeFactory, int depth, String root) {
        this.nodeFactory = nodeFactory;
        this.depth = depth;
        this.root = root;
        this.graph = graph;
    }

    public GraphDisplay getView() {
        return view;
    }

    public String getRoot() {
        return root;
    }

    public int getDepth() {
        return depth;
    }

    public WikiGraphNodeFactory getNodeFactory() {
        return nodeFactory;
    }
    
    public WikiGraph getOriginal() {
        return this.graph;
    }

    public void setOriginal(final WikiGraph graph) {
        this.graph = graph;
    }
}

