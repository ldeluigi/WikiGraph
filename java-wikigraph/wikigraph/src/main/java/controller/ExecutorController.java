package controller;


import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import org.bouncycastle.i18n.LocaleString;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import static controller.ComputeChildrenTask.computeChildren;

public class ExecutorController implements Controller {

    private final int depth;
    private static ForkJoinPool pool;

    public ExecutorController(int depth){
        this.depth= depth;
        pool = ForkJoinPool.commonPool();
    }
    public void compute(String node, HttpWikiGraph nodeFactory,ConcurrentHashMap<String, WikiGraphNode> nodeMap,ForkJoinPool pool){

        pool.invoke(
                new ComputeChildrenTask(null, node,this.depth,nodeFactory,nodeMap));
    }

    public void exit(){
        pool.shutdown();
    }

    @Override
    public void notifyEvent() {
        //check evt
        exit();
    }

    public static void main(String[] args){
        ExecutorController controller = new ExecutorController(3);
        HttpWikiGraph nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        ConcurrentHashMap<String, WikiGraphNode> nodeMap = new ConcurrentHashMap<>();
        controller.compute("Enciclopedia",nodeFactory, nodeMap, pool);
    }
}
