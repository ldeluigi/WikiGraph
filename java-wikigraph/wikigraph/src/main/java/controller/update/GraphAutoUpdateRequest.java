package controller.update;

import model.WikiGraphNodeFactory;
import view.GraphDisplay;

public class GraphAutoUpdateRequest {

    private WikiGraphNodeFactory nodeFactory;
    private int depth;
    private String root;
    private GraphDisplay view = new NoOpView();

    public GraphAutoUpdateRequest(WikiGraphNodeFactory nodeFactory, int depth, String root) {
        this.nodeFactory = nodeFactory;
        this.depth = depth;
        this.root = root;
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
}

