package controller;


import controller.api.HttpWikiGraph;
import model.Pair;

import model.WikiGraphNode;

import view.SwingView;
import view.ViewEvent;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

public class ExecutorController implements Controller {

    private final int DEFAULT_DEPTH =2;
    private int depth;
    private static ForkJoinPool pool;
    private final HttpWikiGraph nodeFactory;
    private final ConcurrentHashMap<String, WikiGraphNode> nodeMap;
    private final SwingView view;

    public ExecutorController(HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap, SwingView view){
        this.view = view;
        this.depth= DEFAULT_DEPTH;
        this.nodeFactory = nodeFactory;
        this.nodeMap = nodeMap;

    }

    public void setDepth(int depth){
        this.depth=depth;
    }

    public int getDepth(){
        return this.depth;
    }
    private void compute(String node){
        pool = ForkJoinPool.commonPool();
        this.pool.invoke(
                new ComputeChildrenTask(null, node,this.depth,this.nodeFactory,this.nodeMap,this.view));
    }

    public void exit(){
        this.pool.shutdown();
    }
    public void computeRandom(){
        WikiGraphNode random = nodeFactory.random();
        compute(random.term());
    }

    public void computeSearch(String node){
        List<Pair<String, String>> res =  this.nodeFactory.search(node);
        compute(res.get(0).getKey());
    }

    @Override
    public void notifyEvent(ViewEvent event) {
        if(event.getType().equals(ViewEvent.EventType.EXIT)){
            exit();
        }else if (event.getType().equals(ViewEvent.EventType.SEARCH)){
            computeSearch("term");//get the term
        }else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)){
            computeRandom();
        }else if (event.getType().equals(ViewEvent.EventType.OTHER)){
            compute("term");//get the term
        }
    }

    public static void main(String[] args){
        HttpWikiGraph nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        ConcurrentHashMap<String, WikiGraphNode> nodeMap = new ConcurrentHashMap<>();
        SwingView view = new SwingView();
        ExecutorController controller = new ExecutorController(nodeFactory,nodeMap,view);
        view.addEventListener(controller);
    }
}
