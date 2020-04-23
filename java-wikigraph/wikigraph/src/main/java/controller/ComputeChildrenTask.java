package controller;

import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String node;
    private final int depth;

    public static void computeChildren(String startNode, int maxDepth){
        new ComputeChildrenTask(null, startNode, maxDepth).invoke();
    }

    public ComputeChildrenTask(CountedCompleter<?> t,String node, int depth) {
        super(t);
        this.node = node;
        this.depth = depth;
    }


    @Override
    public void compute() {
        WikiGraphNodeFactory wikiGraph = new HttpWikiGraph();
        if (depth > 0){
            WikiGraphNode result = wikiGraph.from(this.node);
            for(String child:  result.childrenTerms()){
                addToPendingCount(1);
                new ComputeChildrenTask(this, child,this.depth-1).fork();
            }

        }

        //propagateCompletion();
       tryComplete();
    }

    @Override
    public void onCompletion (CountedCompleter<?> caller) {
        if (caller == this) {
            System.out.printf("completed thread : %s", Thread
                    .currentThread().getName());
        }
    }
}
