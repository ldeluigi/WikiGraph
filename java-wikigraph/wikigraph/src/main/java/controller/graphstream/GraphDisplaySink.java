package controller.graphstream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.stream.Sink;
import view.GraphDisplay;

// TODO mettere i final
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
    public void graphAttributeAdded(final String sourceId, final long timeId, final String attribute, final Object value) {

    }

    @Override
    public void graphAttributeChanged(final String sourceId, final long timeId, final String attribute, final Object oldValue, final Object newValue) {

    }

    @Override
    public void graphAttributeRemoved(final String sourceId, final long timeId, final String attribute) {

    }

    @Override
    public void nodeAttributeAdded(final String sourceId, final long timeId, final String nodeId, final String attribute, final Object value) {

    }

    @Override
    public void nodeAttributeChanged(final String sourceId, final long timeId, final String nodeId, final String attribute, final Object oldValue, final Object newValue) {
        // TODO something
    }

    @Override
    public void nodeAttributeRemoved(final String sourceId, final long timeId, final String nodeId, final String attribute) {

    }

    @Override
    public void edgeAttributeAdded(final String sourceId, final long timeId, final String edgeId, final String attribute, final Object value) {

    }

    @Override
    public void edgeAttributeChanged(final String sourceId, final long timeId, final String edgeId, final String attribute, final Object oldValue, final Object newValue) {

    }

    @Override
    public void edgeAttributeRemoved(final String sourceId, final long timeId, final String edgeId, final String attribute) {

    }

    @Override
    public void nodeAdded(final String sourceId, final long timeId, final String nodeId) {
        this.view.addNode(nodeId, this.finalGraph.getNode(nodeId).getAttribute("depth"), this.lang);
    }

    @Override
    public void nodeRemoved(final String sourceId, final long timeId, final String nodeId) {
        this.view.removeNode(nodeId);
    }

    @Override
    public void edgeAdded(final String sourceId, final long timeId, final String edgeId, final String fromNodeId, final String toNodeId, final boolean directed) {
        this.view.addEdge(fromNodeId, toNodeId);
    }

    @Override
    public void edgeRemoved(final String sourceId, final long timeId, final String edgeId) {
        final Edge e = this.initialGraph.getEdge(edgeId);
        this.view.removeEdge(e.getNode0().getId(), e.getNode1().getId());
    }

    @Override
    public void graphCleared(final String sourceId, final long timeId) {

    }

    @Override
    public void stepBegins(final String sourceId, final long timeId, final double step) {

    }
}
