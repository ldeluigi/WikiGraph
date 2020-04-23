package controller;

import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String node;
    private final int depth;
    private final HttpWikiGraph nodeFactory;

    public static void computeChildren(String startNode, int maxDepth, HttpWikiGraph nodeFactory){
        new ComputeChildrenTask(null, startNode, maxDepth, nodeFactory).invoke();
    }

    public ComputeChildrenTask(CountedCompleter<?> t,String node, int depth, HttpWikiGraph nodeFactory){
        this.nodeFactory = nodeFactory;
        this.node = node;
        this.depth = depth;
    }


    @Override
    public void compute() {
        if (depth > 0){
            WikiGraphNode result = nodeFactory.from(this.node);
            if(result!=null){
                for(String child:  result.childrenTerms()){
                    System.out.println(child);
                    addToPendingCount(1);
                    new ComputeChildrenTask(this, child,this.depth-1,this.nodeFactory).fork();
                }
            }
        }
        propagateCompletion();
       //tryComplete();
    }

    @Override
    public void onCompletion (CountedCompleter<?> caller) {
        if (caller == this) {
            System.out.printf("completed thread : %s", Thread
                    .currentThread().getName());
        }
    }
}
