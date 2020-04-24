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

    public ExecutorController(int depth){
        this.depth= depth;

    }
    public void compute(String node, HttpWikiGraph nodeFactory,ConcurrentHashMap<String, WikiGraphNode> nodeMap){
//        ForkJoinPool.commonPool().invoke(
//                new ComputeChildrenTask(null, node,this.depth,nodeFactory,nodeMap));
//        new ForkJoinPool(1).invoke(
//                new ComputeChildrenTask(null, node,this.depth,nodeFactory,nodeMap));
        computeChildren(node,depth,nodeFactory, nodeMap);
    }

    @Override
    public void notifyEvent() {

    }

    public static void main(String[] args){
        ExecutorController controller = new ExecutorController(2);
        HttpWikiGraph nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        ConcurrentHashMap<String, WikiGraphNode> nodeMap = new ConcurrentHashMap<>();
        controller.compute("Enciclopedia",nodeFactory, nodeMap);
    }
}
