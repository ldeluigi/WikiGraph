package controller;

import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.View;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.locks.Lock;


public class ComputeChildrenTask extends CountedCompleter<Void> {


    private final String myTerm;
    private final int myDepth;
    private final HttpWikiGraph nodeFactory;
    private final ConcurrentWikiGraph graph;
    private final View view;
    private final int maxDepth;
    private final String fatherId;
    private String id;

    public ComputeChildrenTask(ComputeChildrenTask father, String term, int depth, HttpWikiGraph nodeFactory, ConcurrentWikiGraph graph, View view, int maxdepth) {
        super(father);
        this.nodeFactory = nodeFactory;
        this.myTerm = term;
        this.myDepth = depth;
        this.graph = graph;
        this.view = view;
        this.maxDepth = maxdepth;
        this.fatherId = father == null ? "" : father.getNodeId();
    }


    @Override
    public void compute() {
        if (this.graph.isAborted()) {
            tryComplete();
            return;
        }
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
            final Lock lock = this.graph.getLockOn(this.id);
            lock.lock();
            try {
                if (this.graph.contains(this.id)) {
                    this.view.addEdge(this.fatherId, this.id);
                } else {
                    this.view.addNode(this.id, this.myDepth);
                    this.graph.add(result);
                    if (this.myDepth > 0) {
                        view.addEdge(this.fatherId, this.id);
                    }
                    if (this.myDepth < this.maxDepth) {
                        for (String child : result.childrenTerms()) {
                            addToPendingCount(1);
                            new ComputeChildrenTask(this, child, this.myDepth + 1, this.nodeFactory, this.graph, this.view, maxDepth).fork();
                        }
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        tryComplete();
    }

    @Override
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        ex.printStackTrace();
        return false;
    }

    public String getNodeId() {
        return this.id;
    }
}
