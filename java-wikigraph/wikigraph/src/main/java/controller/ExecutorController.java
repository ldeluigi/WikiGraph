package controller;


import static controller.ComputeChildrenTask.computeChildren;

public class ExecutorController implements Controller {

    private final int depth;

    public ExecutorController(int depth){
        this.depth= depth;

    }
    public void compute(String node){
        computeChildren(node,depth);
    }

    @Override
    public void notifyEvent() {

    }

    public static void main(String[] args){
        ExecutorController controller = new ExecutorController(2);
        controller.compute("Enciclopedia");
    }
}
