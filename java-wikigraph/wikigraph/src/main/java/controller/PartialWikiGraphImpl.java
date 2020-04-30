package controller;

import model.MutableGraphImpl;

public class PartialWikiGraphImpl extends MutableGraphImpl implements PartialWikiGraph {

    private boolean aborted;

    @Override
    public void setAborted() {
        this.aborted = true;
    }

    @Override
    public boolean isAborted() {
        return this.aborted;
    }
}
