package controller;


import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.View;
import view.ViewEvent;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

public class ExecutorController implements Controller {

    private static ForkJoinPool pool;
    private HttpWikiGraph nodeFactory;
    private ConcurrentHashMap<String, WikiGraphNode> nodeMap;
    private final View view;

    public ExecutorController(View view) {
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start() {
        this.nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        this.nodeMap = new ConcurrentHashMap<>();
        pool = ForkJoinPool.commonPool();
        view.start();
    }

    private void compute(String node, int depth) {

        CountedCompleter task = new ComputeChildrenTask(null, node, 0, this.nodeFactory, this.nodeMap, this.view, depth);
        this.pool.execute(task);


    }

    private void exit() {
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }


    @Override
    public void notifyEvent(ViewEvent event) {
        if (event.getType().equals(ViewEvent.EventType.EXIT)) {
            this.exit();
        } else if (event.getType().equals(ViewEvent.EventType.SEARCH)) {
            if (this.pool.isQuiescent()) {
                compute(event.getText(), event.getDepth());//get the term
            }
        } else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)) {
            if (this.pool.isQuiescent()) {
                compute(null, event.getDepth());
            }
        } else if (event.getType().equals(ViewEvent.EventType.OTHER)) {
            //compute("term");//get the term
        }
    }


}
