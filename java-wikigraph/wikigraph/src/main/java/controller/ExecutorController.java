package controller;


import java.util.concurrent.ForkJoinPool;

import static controller.ComputeChildrenTask.computeChildren;

public class ExecutorController implements Controller {

    private final int depth;

    public ExecutorController(int depth){
        this.depth= depth;

    }
    public void compute(String node){
//        ForkJoinPool.commonPool().invoke(
//                new ComputeChildrenTask(null, node,this.depth));
        computeChildren(node,depth);
    }

    @Override
    public void notifyEvent() {

    }

    public static void main(String[] args){
        ExecutorController controller = new ExecutorController(6);
        controller.compute("Enciclopedia");
    }
}
