package controller;


import static controller.ComputeChildrenTask.computeChildren;

public class ExecutorController implements Controller {

    private final int depth;

    public ExecutorController(int depth, int poolSize){
        this.depth= depth;

    }
    public void compute(String node){
        computeChildren(node,depth);
    }

    @Override
    public void notifyEvent() {

    }
}
