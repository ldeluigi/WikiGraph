package controller;

import model.GraphNode;
import model.HttpWikiGraph;
import model.WikiGraph;

import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String node;
    private final int depth;

    public static void computeChildren(String startNode, int maxDepth){
        new ComputeChildrenTask(null, startNode, maxDepth);
    }

    public ComputeChildrenTask(CountedCompleter<?> t,String node, int depth) {
        this.node = node;
        this.depth = depth;
    }


    @Override
    public void compute() {
        WikiGraph wikiGraph = new HttpWikiGraph();
        if (depth > 0){
            GraphNode result = wikiGraph.from(node);
            for(String child:  result.childrenTerms()){
                new ComputeChildrenTask(this, child,this.depth-1);
            }
        }
        propagateCompletion();
    }
}
