package controller;

import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.View;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String node;
    private final int depth;
    private final HttpWikiGraph nodeFactory;
    private final ConcurrentHashMap<String, WikiGraphNode> nodeMap;
    private final View view;
    private final Boolean first;

    public static void computeChildren(String startNode, int maxDepth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap, View view) {
        new ComputeChildrenTask(null, startNode, maxDepth, nodeFactory, nodeMap, view, true).invoke();
    }

    public ComputeChildrenTask(CountedCompleter<?> t, String node, int depth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap, View view, boolean first) {
        super(t);
        this.nodeFactory = nodeFactory;
        this.node = node;
        this.depth = depth;
        this.nodeMap = nodeMap;
        this.view = view;
        this.first = first;
    }


    @Override
    public void compute() {
        final WikiGraphNode result;

        if (this.first) {
            if (this.node == null) {//random
                result = nodeFactory.random();
            } else {//search
                result = this.nodeFactory.from(this.node);
            }
            view.addNode(result.term(),this.depth);
        } else {
            result = nodeFactory.from(this.node);
        }


        if (depth > 0 && result != null) {
            if (this.nodeMap.put(result.term(), result) == null) {
                for (String child : result.childrenTerms()) {
                    view.addNode(child,this.depth);
                    view.addEdge(result.term(), child);
                    addToPendingCount(1);
                    new ComputeChildrenTask(this, child, this.depth - 1, this.nodeFactory, this.nodeMap, this.view, false).fork();
                }
            } else {
                for (String child : result.childrenTerms()) {
                    view.addEdge(result.term(), child); //aggiungere arco
                }
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
