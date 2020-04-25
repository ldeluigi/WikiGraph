package start;

import controller.ExecutorController;
import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.SwingView;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class WikiGraphStart {

    public static void main(String[] args){

        SwingView view = new SwingView();
        ExecutorController controller = new ExecutorController(view);
        controller.start();
    }

}
