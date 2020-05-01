package controller.paradigm.concurrent;

import controller.PartialWikiGraph;
import model.MutableGraphImpl;

import java.util.concurrent.atomic.AtomicBoolean;

public class PartialWikiGraphImpl extends MutableGraphImpl implements PartialWikiGraph {

    private AtomicBoolean aborted = new AtomicBoolean(false);

    @Override
    public void setAborted() {
        this.aborted.set(true);
    }

    @Override
    public boolean isAborted() {
        return this.aborted.get();
    }
}
