package controller;


import controller.api.HttpWikiGraph;
import model.Pair;
import model.WikiGraphNode;
import org.bouncycastle.i18n.LocaleString;
import view.ViewEvent;

import java.util.List;
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
    public void compute(String node, HttpWikiGraph nodeFactory,ConcurrentHashMap<String, WikiGraphNode> nodeMap){
        this.pool.invoke(
                new ComputeChildrenTask(null, node,this.depth,nodeFactory,nodeMap));
    }

    public void exit(){
        pool.shutdown();
    }
    public void computeRandom(){

    }

    public void computeSearch(String node, HttpWikiGraph nodeFactory,ConcurrentHashMap<String, WikiGraphNode> nodeMap){
        List<Pair<String, String>> res =  nodeFactory.search(node);
        compute(res.get(0).getKey(), nodeFactory, nodeMap);
    }

    @Override
    public void notifyEvent(ViewEvent event) {
        //check evt
        exit();
    }

    public static void main(String[] args){
        ExecutorController controller = new ExecutorController(2);
        HttpWikiGraph nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        ConcurrentHashMap<String, WikiGraphNode> nodeMap = new ConcurrentHashMap<>();
        controller.compute("Enciclopedia",nodeFactory, nodeMap);
    }
}
