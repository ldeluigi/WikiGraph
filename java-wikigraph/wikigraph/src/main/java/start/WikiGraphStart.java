package start;

import controller.ExecutorController;
import view.SwingView;
import view.View;


public class WikiGraphStart {

    public static void main(String[] args) {
        View view = new SwingView();
        ExecutorController controller = new ExecutorController(view);
        controller.start();
    }

}
