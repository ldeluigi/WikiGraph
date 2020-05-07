package controller.update;

import org.graphstream.stream.SourceBase;
import view.GraphDisplay;

/**
 * A view that does nothing.
 */
public class NoOpView implements GraphDisplay {
    @Override
    public void addNode(final String id, final int depth, final String lang) {
    }

    @Override
    public void updateDepthNode(String id, int depth) {
    }


    @Override
    public void addEdge(final String idFrom, final String idTo) {
    }

    @Override
    public void removeNode(final String id) {
    }

    @Override
    public void removeEdge(final String idFrom, final String idTo) {
    }

    @Override
    public void clearGraph() {
    }
}
