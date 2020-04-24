package controller;

import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String node;
    private final int depth;
    private final HttpWikiGraph nodeFactory;
    private final ConcurrentHashMap<String, WikiGraphNode> nodeMap;

    public static void computeChildren(String startNode, int maxDepth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap) {
        new ComputeChildrenTask(null, startNode, maxDepth, nodeFactory, nodeMap).invoke();
    }

    public ComputeChildrenTask(CountedCompleter<?> t, String node, int depth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap) {
        super(t);
        this.nodeFactory = nodeFactory;
        this.node = node;
        this.depth = depth;
        this.nodeMap = nodeMap;
    }


    @Override
    public void compute() {
        if (depth > 0) {
            WikiGraphNode result = nodeFactory.from(this.node);
            if (result != null) {
                if (this.nodeMap.put(result.term(), result) == null) {
                    for (String child : result.childrenTerms()) {
                       //System.out.println(child);
                        addToPendingCount(1);
                        new ComputeChildrenTask(this, child, this.depth - 1, this.nodeFactory, this.nodeMap).fork();
                    }
                } else {
                    //aggiungere arco
                }
            }
        }
        //propagateCompletion();
        tryComplete();
    }

    @Override
    public void onCompletion (CountedCompleter<?> caller) {
        if (caller == this) {
            System.out.printf("completed thread : %s ", Thread
                    .currentThread().getName());
        }
    }

}
