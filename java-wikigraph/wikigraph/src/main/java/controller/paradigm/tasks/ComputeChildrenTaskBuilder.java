package controller.paradigm.tasks;

import controller.ConcurrentWikiGraph;
import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.View;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.locks.Lock;


public class ComputeChildrenTaskBuilder {


    private String myTerm;
    private int myDepth;
    private HttpWikiGraph nodeFactory;
    private ConcurrentWikiGraph graph;
    private View view;
    private int maxDepth;
    private String fatherId;
    private String id;

    public ComputeChildrenTaskBuilder() { }

    public ComputeChildrenTaskBuilder setFather(ComputeChildrenTaskBuilder father){
        this.fatherId = father == null ? "" : father.getNodeId();
        return this;
    }

    public ComputeChildrenTaskBuilder setTerm(String term){
        this.myTerm = term;
        return this;
    }

    public ComputeChildrenTaskBuilder setDepth(int depth){
        this.myDepth = depth;
        return this;
    }

    public ComputeChildrenTaskBuilder setNodeFactory(HttpWikiGraph nodeFactory){
        this.nodeFactory = nodeFactory;
        return this;
    }

    public ComputeChildrenTaskBuilder setNodeFactory(ConcurrentWikiGraph graph){
        this.graph = graph;
        return this;
    }

    public ComputeChildrenTaskBuilder setNodeFactory(View view){
        this.view = view;
        return this;
    }

    public ComputeChildrenTaskBuilder setMaxDepth(int maxDepth){
        this.maxDepth = maxDepth;
        return this;
    }

    public String getNodeId() {
        return this.id;
    }
}
