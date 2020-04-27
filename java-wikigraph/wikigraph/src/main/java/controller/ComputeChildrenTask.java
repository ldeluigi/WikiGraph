package controller;

import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.View;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String myTerm;
    private final int myDepth;
    private final HttpWikiGraph nodeFactory;
    private final ConcurrentHashMap<String, WikiGraphNode> nodeMap;
    private final View view;
    private final int maxDepth;
    private final String fatherId;
    private String id;

    public ComputeChildrenTask(ComputeChildrenTask father, String term, int depth, HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap, View view, int maxdepth) {
        super(father);
        this.nodeFactory = nodeFactory;
        this.myTerm = term;
        this.myDepth = depth;
        this.nodeMap = nodeMap;
        this.view = view;
        this.maxDepth = maxdepth;
        this.fatherId = father == null ? "" : father.getNodeId();
    }


    @Override
    public void compute() {
        final WikiGraphNode result;
        if (this.myDepth == 0) {
            if (this.myTerm == null) { //random
                result = nodeFactory.random();
            } else { //search
                result = this.nodeFactory.from(this.myTerm);
            }
        } else {
            result = nodeFactory.from(this.myTerm);
        }
        if (result != null) {
            this.id = result.term();
            if (this.nodeMap.putIfAbsent(this.id, result) == null) {
                view.addNode(this.id, this.myDepth);
                if (this.myDepth > 0) {
                    view.addEdge(this.fatherId, this.id);
                }
                if (this.myDepth < this.maxDepth) {
                    for (String child : result.childrenTerms()) {
                        addToPendingCount(1);
                        new ComputeChildrenTask(this, child, this.myDepth + 1, this.nodeFactory, this.nodeMap, this.view, maxDepth).fork();
                    }
                }
            } else if (this.myDepth > 0) {
                view.addEdge(this.fatherId, this.id);
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

    public String getNodeId() {
        return this.id;
    }
}
