package controller.paradigm.tasks;

import controller.ConcurrentWikiGraph;
import controller.api.HttpWikiGraph;
import model.WikiGraphNodeFactory;
import view.View;


public class ComputeChildrenTaskBuilder {


    private String term;
    private WikiGraphNodeFactory nodeFactory;
    private ConcurrentWikiGraph graph;
    private View view;
    private int maxDepth = -1;

    public ComputeChildrenTaskBuilder() { }

    public ComputeChildrenTaskBuilder setTerm(String term){
        this.term = term;
        return this;
    }

    public ComputeChildrenTaskBuilder setNodeFactory(HttpWikiGraph nodeFactory){
        this.nodeFactory = nodeFactory;
        return this;
    }

    public ComputeChildrenTaskBuilder setGraph(ConcurrentWikiGraph graph){
        this.graph = graph;
        return this;
    }

    public ComputeChildrenTaskBuilder setView(View view){
        this.view = view;
        return this;
    }

    public ComputeChildrenTaskBuilder setMaxDepth(int maxDepth){
        if (maxDepth<0) {
            throw new IllegalArgumentException();
        }
        this.maxDepth = maxDepth;
        return this;
    }


    public ComputeChildrenTask build(){
        if (this.nodeFactory == null ||
                this.graph == null ||
                this.view == null ||
                this.maxDepth == -1 ||
                this.term == null)
            throw new IllegalStateException();
        return new ComputeChildrenTask(this.nodeFactory, this.graph, this.view, this.maxDepth, this.term);
    }

}
