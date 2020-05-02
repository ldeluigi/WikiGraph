package start;

import controller.Controller;
import controller.paradigm.eventloop.EventLoopController;
import controller.paradigm.tasks.ExecutorController;
import view.SwingView;
import view.View;


public class WikiGraphStart {

    public static void main(String[] args) {
        View view = new SwingView();
        Controller controller = new ExecutorController(view);
        controller.start();
    }

}
