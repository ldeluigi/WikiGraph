package view;

import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.util.GraphDiff;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Iterator;

public class GraphDiffTest {

    @Test
    void testGraphDiff() {
        Graph firstGraph = new MultiGraph("WikiGraph");
        addNode(firstGraph,"root",0,"en");
        addNode(firstGraph,"child1",1,"en");
        addNode(firstGraph,"child2",1,"en");
        addNode(firstGraph,"grandson11",2,"en");
        addNode(firstGraph,"grandson12",2,"en");
        addNode(firstGraph,"grandson13",2,"en");
        addEdge(firstGraph,"root","child1");
        addEdge(firstGraph,"root","child2");
        addEdge(firstGraph,"child1","grandson11");
        addEdge(firstGraph,"child1","grandson12");
        addEdge(firstGraph,"child1","grandson13");
        Assert.assertEquals(6, firstGraph.getNodeCount());
        Assert.assertEquals(5, firstGraph.getEdgeCount());
        Graph newGraph = new MultiGraph("WikiGraph");
        addNode(newGraph,"root",0,"en");
        addNode(newGraph,"child1",1,"en");
        addNode(newGraph,"child2",1,"en");
        addNode(newGraph,"grandson21",2,"en");
        addNode(newGraph,"grandson22",2,"en");
        addEdge(newGraph,"root","child1");
        addEdge(newGraph,"root","child2");
        addEdge(newGraph,"child2","grandson21");
        addEdge(newGraph,"child2","grandson22");
        Assert.assertEquals(5, newGraph.getNodeCount());
        Assert.assertEquals(4, newGraph.getEdgeCount());
        try{
            new GraphDiff(firstGraph, newGraph).apply(firstGraph);
        } catch (ElementNotFoundException e){
            System.out.println("element already removed");
        }

        Assert.assertEquals(firstGraph.getNodeCount(), newGraph.getNodeCount());
        Assert.assertEquals(firstGraph.getEdgeCount(), newGraph.getEdgeCount());
}


    private void addNode(final Graph graph, final String id, final int depth, final String lang) {
            if (graph.getNode(id) == null) {
                graph.addNode(id);
                final Node n = graph.getNode(id);
                n.addAttribute("ui.class", "d" + depth);
                n.addAttribute("label", id);
                n.addAttribute("lang", lang);
            } else {
                System.err.println("INFO: DUPLICATE NODE IGNORED - " + id);
            }
    }

    private void addEdge(final Graph graph, final String idFrom, final String idTo) {
            final Node from = graph.getNode(idFrom);
            if (from == null) {
                System.err.println("ERROR: node " + idFrom + " not found. Aborting edge " + idFrom + "@@@" + idTo);
                return;
            }
            final Node to = graph.getNode(idTo);
            if (to == null) {
                System.err.println("ERROR: node " + idTo + " not found. Aborting edge " + idFrom + "@@@" + idTo);
                return;
            }
            final String name = idFrom + "@@@" + idTo;
            if (graph.getEdge(name) == null) {
                graph.addEdge(idFrom + "@@@" + idTo, from, to, true);
            }
    }
}
