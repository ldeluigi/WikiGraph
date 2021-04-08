package start;

import controller.Controller;
import controller.paradigm.eventloop.EventLoopController;
import controller.paradigm.reactivex.ReactiveXController;
import controller.paradigm.tasks.ExecutorController;
import view.SwingView;


public class WikiGraphStart {

    public static void main(String[] args) {
        Controller controller = null;
        if (args.length > 0) {
            switch (args[0]) {
                case "executors":
                    controller = new ExecutorController(new SwingView());
                    break;
                case "eventloop":
                    controller = new EventLoopController(new SwingView());
                    break;
                case "rx":
                    controller = new ReactiveXController(new SwingView());
                    break;
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
