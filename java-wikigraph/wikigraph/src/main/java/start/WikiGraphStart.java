package start;

import controller.ExecutorController;
import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.SwingView;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class WikiGraphStart {

    public static void main(String[] args){
        HttpWikiGraph nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        ConcurrentHashMap<String, WikiGraphNode> nodeMap = new ConcurrentHashMap<>();
        SwingView view = new SwingView();
        ExecutorController controller = new ExecutorController(nodeFactory,nodeMap,view);
        view.addEventListener(controller);
    }

}
