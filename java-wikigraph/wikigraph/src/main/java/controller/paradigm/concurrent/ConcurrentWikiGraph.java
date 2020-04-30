package controller.paradigm.concurrent;

import controller.PartialWikiGraph;
import model.MutableWikiGraph;

import java.util.concurrent.locks.Lock;

public interface ConcurrentWikiGraph extends PartialWikiGraph {

    /**
     * Returns a lock that grants mutual exclusion between threads that are computing the exact
     * same term at the same time. This is useful if you need to create nodes before edges, without
     * duplicate nodes, based on the terms and their children.
     * @param nodeTerm the term that is paired with the lock
     * @return the lock paired with input term
     */
    Lock getLockOn(final String nodeTerm);
}
