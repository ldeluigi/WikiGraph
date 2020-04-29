package controller.paradigm.tasks;

import controller.ConcurrentWikiGraph;
import controller.api.HttpWikiGraph;
import view.View;


public class ComputeChildrenTaskBuilder {


    private String term;
    private int actualDepth;
    private HttpWikiGraph nodeFactory;
    private ConcurrentWikiGraph graph;
    private View view;
    private int maxDepth;
    private ComputeChildrenTask father;

    public ComputeChildrenTaskBuilder() { }

    public ComputeChildrenTaskBuilder setFather(ComputeChildrenTask father){
        this.father = father;
        return this;
    }

    public ComputeChildrenTaskBuilder setTerm(String term){
        this.term = term;
        return this;
    }

    public ComputeChildrenTaskBuilder setDepth(int depth){
        if (depth<0) {
            throw new IllegalArgumentException();
        }
        this.actualDepth = depth;
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
             this.father == null ||
             this.term == null ||
             this.maxDepth == -1 ||
             this.actualDepth == -1)
            throw new IllegalStateException();
        return new ComputeChildrenTask(father, term, actualDepth, nodeFactory, graph, view, maxDepth);
    }

}
