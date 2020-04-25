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

    public static void computeChildren(String startNode, int maxDepth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap,View view) {
        new ComputeChildrenTask(null, startNode, maxDepth, nodeFactory, nodeMap,view,true).invoke();
    }

    public ComputeChildrenTask(CountedCompleter<?> t, String node, int depth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap,View view, boolean first) {
        super(t);
        this.nodeFactory = nodeFactory;
        this.node = node;
        this.depth = depth;
        this.nodeMap = nodeMap;
        this.view =view;
        this.first = first;
    }


    @Override
    public void compute() {
        if (this.first){
            view.addNode(this.node);
        }
        if (depth > 0) {
            WikiGraphNode result = nodeFactory.from(this.node);
            if (result != null) {
                if (this.nodeMap.put(result.term(), result) == null) {
                    for (String child : result.childrenTerms()) {
                        view.addNode(child);

                       //System.out.println(child);
                        addToPendingCount(1);
                        new ComputeChildrenTask(this, child, this.depth - 1, this.nodeFactory, this.nodeMap,this.view,false).fork();
                    }
                } else {
                    for (String child : result.childrenTerms()){
                        view.addEdge(result.term(),child); //aggiungere arco
                    }
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
