package controller;


import controller.api.HttpWikiGraph;
import model.WikiGraphNode;
import view.View;
import view.ViewEvent;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class ExecutorController implements Controller {

    private ForkJoinPool pool;
    private HttpWikiGraph nodeFactory;
    private final View view;

    public ExecutorController(View view) {
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start() {
        this.nodeFactory = new HttpWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        pool = ForkJoinPool.commonPool();
        view.start();
    }

    private void compute(String node, int depth, final ConcurrentWikiGraph last) {
        this.pool.execute(new ComputeChildrenTask(null, node, 0, this.nodeFactory, last, this.view, depth));
    }

    private void exit() {
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }

    private ConcurrentWikiGraph last = null;

    @Override
    public void notifyEvent(ViewEvent event) {
        if (event.getType().equals(ViewEvent.EventType.EXIT)) {
            this.exit();
        } else if (event.getType().equals(ViewEvent.EventType.SEARCH)) {
            if (this.last != null) {
                this.last.setAborted();
                this.last = null;
                // await quiescence to avoid errors
                // but it's blocking! F
            }
            if (this.pool.awaitQuiescence(100, TimeUnit.MILLISECONDS)) {
                this.last = SynchronizedWikiGraph.empty();
                compute(event.getText(), event.getDepth(), this.last);
            }
        } else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)) {
            if (this.last != null) {
                this.last.setAborted();
                this.last = null;
                // await quiescence to avoid errors
                // but it's blocking! F
            }
            if (this.pool.awaitQuiescence(100, TimeUnit.MILLISECONDS)) {
                this.last = SynchronizedWikiGraph.empty();
                compute(null, event.getDepth(), this.last);
            }
        }
    }


}
