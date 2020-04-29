package controller.paradigm.tasks;

import controller.ConcurrentWikiGraph;
import controller.api.HttpWikiGraph;
import controller.paradigm.NodeRecursion;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.concurrent.CountedCompleter;
import java.util.concurrent.locks.Lock;


public class ComputeChildrenTask extends CountedCompleter<Void> {

    private final NodeRecursion nr;

    private ComputeChildrenTask(final ComputeChildrenTask father, final NodeRecursion rec) {
        super(father);
        this.nr = rec;
    }

    public ComputeChildrenTask(WikiGraphNodeFactory factory, ConcurrentWikiGraph graph,
                               View view, int maxDepth, String term) {
        this.nr = new TaskNodeRecursion(factory, graph, view, maxDepth, term);
    }

    @Override
    public void compute() {
        this.nr.compute();
    }

    @Override
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        ex.printStackTrace();
        return false;
    }

    private class TaskNodeRecursion extends NodeRecursion {

        private TaskNodeRecursion(NodeRecursion father, String term) {
            super(father, term);
        }

        public TaskNodeRecursion(WikiGraphNodeFactory factory, ConcurrentWikiGraph graph,
                                 View view, int maxDepth, String term) {
            super(factory, graph, view, maxDepth, term);
        }

        @Override
        protected void complete() {
            tryComplete();
        }

        @Override
        protected void scheduleChild(String term) {
            addToPendingCount(1);
            new ComputeChildrenTask(ComputeChildrenTask.this,
                    new TaskNodeRecursion(this, term)).fork();
        }

        @Override
        public void abort() {
            tryComplete();
        }
    }
}
