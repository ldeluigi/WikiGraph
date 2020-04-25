package controller;

import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.View;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String node;
    private final int myDepth;
    private final HttpWikiGraph nodeFactory;
    private final ConcurrentHashMap<String, WikiGraphNode> nodeMap;
    private final View view;
    private final int maxDepth;

    public ComputeChildrenTask(CountedCompleter<?> t, String node, int depth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap, View view, int maxdepth) {
        super(t);
        this.nodeFactory = nodeFactory;
        this.node = node;
        this.myDepth = depth;
        this.nodeMap = nodeMap;
        this.view = view;
        this.maxDepth = maxdepth;
    }


    @Override
    public void compute() {
        final WikiGraphNode result;

        if (this.myDepth == 0) {
            if (this.node == null) { //random
                result = nodeFactory.random();
            } else { //search
                result = this.nodeFactory.from(this.node);
            }
            view.addNode(result.term(), this.myDepth);
        } else {
            result = nodeFactory.from(this.node);
        }


        if (myDepth < maxDepth && result != null) {
            if (this.nodeMap.put(result.term(), result) == null) {
                for (String child : result.childrenTerms()) {
                    view.addNode(child, myDepth + 1);
                    view.addEdge(result.term(), child);
                    addToPendingCount(1);
                    new ComputeChildrenTask(this, child, this.myDepth + 1, this.nodeFactory, this.nodeMap, this.view, maxDepth).fork();
                }
            } else {
                for (String child : result.childrenTerms()) {
                    view.addEdge(result.term(), child);}
            }
        }
        propagateCompletion();
        //tryComplete();
    }

    @Override
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        ex.printStackTrace();
        return false;
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        if (caller == this) {
            System.out.printf("completed thread : %s ", Thread
                    .currentThread().getName());
        }
    }

}
