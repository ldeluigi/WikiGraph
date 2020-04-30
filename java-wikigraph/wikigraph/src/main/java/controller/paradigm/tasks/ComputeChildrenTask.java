package controller.paradigm.tasks;

import controller.paradigm.concurrent.ConcurrentNodeRecursion;
import controller.paradigm.concurrent.ConcurrentWikiGraph;
import model.WikiGraphNodeFactory;
import view.View;

import java.util.concurrent.CountedCompleter;


public class ComputeChildrenTask extends CountedCompleter<Void> {

    private final ConcurrentNodeRecursion nr;

    private ComputeChildrenTask(final ComputeChildrenTask father, final ConcurrentNodeRecursion rec) {
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

    private class TaskNodeRecursion extends ConcurrentNodeRecursion {

        private TaskNodeRecursion(final ConcurrentNodeRecursion father, final String term) {
            super(father, term);
        }

        public TaskNodeRecursion(final WikiGraphNodeFactory factory, final ConcurrentWikiGraph graph,
                                 final View view, final int maxDepth, final String term) {
            super(factory, graph, view, maxDepth, term);
        }

        @Override
        protected void complete() {
            tryComplete();
        }

        @Override
        protected void childBirth(final String term) {
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
