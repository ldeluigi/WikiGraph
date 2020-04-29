package controller.paradigm;

import controller.ConcurrentWikiGraph;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import model.MutableWikiGraph;
import model.WikiGraph;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.Optional;

public  class NodeRecursionBrutto {

    private final WikiGraphNodeFactory factory;
    private final MutableWikiGraph graph;
    private final View view;
    private final int maxDepth;
    private final String term;
    private final String fatherID;
    private final int depth;
    private final Vertx vertx;
    private Optional<String> id = Optional.empty();

    protected NodeRecursionBrutto(final NodeRecursionBrutto father, final String term) {
        this.factory = father.getNodeFactory();
        this.graph = father.getGraph();
        this.view = father.getView();
        this.maxDepth = father.getMaxDepth();
        this.term = term;
        this.vertx = father.getVertx();
        if (father.getID().isEmpty()) {
            throw new IllegalArgumentException("Father has not yet computed");
        }
        this.fatherID = father.getID().get();
        this.depth = father.getDepth() + 1;
    }

    public Vertx getVertx() {
        return this.vertx;
    }

    public NodeRecursionBrutto(final WikiGraphNodeFactory factory,
                                  final MutableWikiGraph graph,
                                  final View view,
                                  final int maxDepth,
                                  final String term,
                                  final Vertx vertx) {
        this.factory = factory;
        this.graph = graph;
        this.view = view;
        this.maxDepth = maxDepth;
        this.term = term;
        this.fatherID = null;
        this.depth = 0;
        this.vertx=vertx;
    }

    public void compute() {
        if (this.depth == 0) {
            vertx.executeBlocking((Promise<WikiGraphNode> promise) ->{
                promise.complete(initRoot());
            }).onSuccess(res->{
                cose(res);
            });
        } else {
            vertx.executeBlocking((Promise<WikiGraphNode> promise)->{
                promise.complete(this.factory.from(this.term));
            }).onSuccess(res->{
                cose(res);
            });
        }

    }
    private void cose(WikiGraphNode result){
        if (result != null) {
            this.id = Optional.of(result.term());

            if (this.graph.contains(this.id.get())) {
                this.view.addEdge(this.fatherID, this.id.get());
            } else {

                this.view.addNode(this.id.get(), this.depth, this.factory.getLanguage());
                this.graph.add(result);
                if (this.depth > 0) {
                    view.addEdge(this.fatherID, this.id.get());
                }
                if (this.depth < this.maxDepth) {
                    for (String child : result.childrenTerms()) {
                        childBirth(child);
                    }
                }
            }

        }

        complete();
    }

    protected  void complete(){

    };

    protected  void childBirth(final String term){
        vertx.runOnContext(promise ->{
            new NodeRecursionBrutto(this,term).compute();
        });
    };

    protected WikiGraphNode initRoot() {
        if (this.term == null) { //random
            return this.factory.random();
        } else { //search
            return this.factory.from(this.term);
        }
    }

    public int getDepth() {
        return this.depth;
    }

    public String getTerm() {
        return this.term;
    }

    public WikiGraphNodeFactory getNodeFactory() {
        return this.factory;
    }

    public MutableWikiGraph getGraph() {
        return this.graph;
    }

    public View getView() {
        return this.view;
    }

    public int getMaxDepth() {
        return this.maxDepth;
    }

    public Optional<String> getID() {
        return this.id;
    }
}
