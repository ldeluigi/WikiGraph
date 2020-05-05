package controller.update;

import view.GraphDisplay;

/**
 * A view that does nothing.
 */
public class NoOpView implements GraphDisplay {
    @Override
    public void addNode(String id, int depth, String lang) {
    }

    @Override
    public void addEdge(String idFrom, String idTo) {
    }

    @Override
    public void removeNode(String id) {
    }

    @Override
    public void removeEdge(String idFrom, String idTo) {
    }

    @Override
    public void clearGraph() {
    }
}
