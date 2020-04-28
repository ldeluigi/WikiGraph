package controller;

import model.MutableWikiGraph;

import java.util.concurrent.locks.Lock;

public interface ConcurrentWikiGraph extends MutableWikiGraph {
    void setAborted();

    boolean isAborted();

    Lock getLockOn(final String nodeTerm);
}
