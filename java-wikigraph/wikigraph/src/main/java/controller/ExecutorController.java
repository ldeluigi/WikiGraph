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

    private static ForkJoinPool pool;
    private final HttpWikiGraph nodeFactory;
    private final ConcurrentHashMap<String, WikiGraphNode> nodeMap;
    private final SwingView view;

    public ExecutorController(HttpWikiGraph nodeFactory, ConcurrentHashMap<String, WikiGraphNode> nodeMap, SwingView view){
        this.view = view;
        this.nodeFactory = nodeFactory;
        this.nodeMap = nodeMap;

    }

    private void compute(String node, int depth){
        pool = ForkJoinPool.commonPool();
        this.pool.invoke(
                new ComputeChildrenTask(null, node,depth,this.nodeFactory,this.nodeMap,this.view,true));
    }

    public void exit(){
        this.pool.shutdown();
    }
    public void computeRandom(int depth){
        WikiGraphNode random = nodeFactory.random();
        compute(random.term(),depth);
    }

    public void computeSearch(String node, int depth){
        List<Pair<String, String>> res =  this.nodeFactory.search(node);
        compute(res.get(0).getKey(),depth);
    }

    @Override
    public void notifyEvent(ViewEvent event) {
        if(event.getType().equals(ViewEvent.EventType.EXIT)){
            exit();
        }else if (event.getType().equals(ViewEvent.EventType.SEARCH)){
            computeSearch(event.getText(),event.getDepth());//get the term
        }else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)){
            computeRandom(event.getDepth());
        }else if (event.getType().equals(ViewEvent.EventType.OTHER)){
            //compute("term");//get the term
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
