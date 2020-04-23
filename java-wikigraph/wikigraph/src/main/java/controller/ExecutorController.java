package controller;


import controller.api.HttpWikiGraph;
import org.bouncycastle.i18n.LocaleString;

import java.util.Locale;
import java.util.concurrent.ForkJoinPool;

import static controller.ComputeChildrenTask.computeChildren;

public class ExecutorController implements Controller {

    private final int depth;

    public ExecutorController(int depth){
        this.depth= depth;

    }
    public void compute(String node, HttpWikiGraph nodeFactory){
//        ForkJoinPool.commonPool().invoke(
//                new ComputeChildrenTask(null, node,this.depth));
        computeChildren(node,depth,nodeFactory);
    }

    @Override
    public void notifyEvent() {

    }

    public static void main(String[] args){
        ExecutorController controller = new ExecutorController(2);
        HttpWikiGraph nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ITALIAN.getLanguage());
        controller.compute("Enciclopedia",nodeFactory);
    }
}
