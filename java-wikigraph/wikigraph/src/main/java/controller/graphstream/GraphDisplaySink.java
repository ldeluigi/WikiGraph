package controller.graphstream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.stream.Sink;
import view.GraphDisplay;

public class GraphDisplaySink implements Sink {
    public static final String DEPTH_ATTRIBUTE = "depth";


    private final GraphDisplay view;
    private final String lang;
    private final Graph finalGraph;
    private final Graph initialGraph;

    public GraphDisplaySink(final GraphDisplay output, final Graph initialGraph, final Graph finalGraph, final String langCode) {
        this.finalGraph = finalGraph;
        this.initialGraph = initialGraph;
        this.view = output;
        this.lang = langCode;
    }

    @Override
    public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) {

    }

    @Override
    public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) {

    }

    @Override
    public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {

    }

    @Override
    public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {

    }

    @Override
    public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {
        // TODO something
    }

    @Override
    public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {

    }

    @Override
    public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) {

    }

    @Override
    public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue, Object newValue) {

    }

    @Override
    public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) {

    }

    @Override
    public void nodeAdded(String sourceId, long timeId, String nodeId) {
        this.view.addNode(nodeId, this.finalGraph.getNode(nodeId).getAttribute("depth"), this.lang);
    }

    @Override
    public void nodeRemoved(String sourceId, long timeId, String nodeId) {
        this.view.removeNode(nodeId);
    }

    @Override
    public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
        this.view.addEdge(fromNodeId, toNodeId);
    }

    @Override
    public void edgeRemoved(String sourceId, long timeId, String edgeId) {
        final Edge e = this.initialGraph.getEdge(edgeId);
        final String id0 = e.getNode0().getId();
        final String id1 = e.getNode1().getId();
        this.view.removeEdge(id0, id1);
    }

    @Override
    public void graphCleared(String sourceId, long timeId) {

    }

    @Override
    public void stepBegins(String sourceId, long timeId, double step) {

    }
}
