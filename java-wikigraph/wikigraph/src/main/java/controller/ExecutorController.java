package controller;


import controller.api.HttpWikiGraph;
import model.Pair;

import model.WikiGraphNode;


import view.View;
import view.ViewEvent;


import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import static java.lang.Runtime.*;

public class ExecutorController implements Controller {

    private static ForkJoinPool pool;
    private  HttpWikiGraph nodeFactory;
    private  ConcurrentHashMap<String, WikiGraphNode> nodeMap;
    private final View view;

    public ExecutorController(View view){
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start(){
        this.nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        this.nodeMap = new ConcurrentHashMap<>();
        pool = ForkJoinPool.commonPool();
        view.start();
    }

    private void compute(String node, int depth){
        this.pool.execute(
                new ComputeChildrenTask(null, node,depth,this.nodeFactory,this.nodeMap, this.view,true));
    }

    private void exit(){
        if(this.pool!= null){
            this.pool.shutdown();
        }
    }


    @Override
    public void notifyEvent(ViewEvent event) {
        if(event.getType().equals(ViewEvent.EventType.EXIT)){
            this.exit();
        }else if (event.getType().equals(ViewEvent.EventType.SEARCH)){
            compute(event.getText(),event.getDepth());//get the term
        }else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)){
            compute(null,event.getDepth());
        }else if (event.getType().equals(ViewEvent.EventType.OTHER)){
            //compute("term");//get the term
        }
    }


}
