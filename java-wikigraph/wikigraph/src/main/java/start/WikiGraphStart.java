package start;

import controller.Controller;
import controller.paradigm.eventloop.EventLoopController;
import controller.paradigm.reactivex.ReactiveXController;
import controller.paradigm.tasks.ExecutorController;
import view.SwingView;
import view.View;


public class WikiGraphStart {

    public static void main(String[] args) {
        final View view = new SwingView();
        Controller controller = null;
        if (args.length > 0) {
            if (args[0].equals("executors")) {
                controller = new ExecutorController(view);
            } else if (args[0].equals("eventloop")) {
                controller = new EventLoopController(view);
            } else if (args[0].equals("rx")) {
                controller = new ReactiveXController(view);
            }
        }
        if (controller == null) {
            printHelp();
        } else {
            controller.start();
        }
    }

    private static void printHelp() {
        System.out.println("Usage: [executors|eventloop|rx]");
    }
}
