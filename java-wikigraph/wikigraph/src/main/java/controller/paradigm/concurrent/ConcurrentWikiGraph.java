package controller.paradigm.concurrent;

import java.util.concurrent.locks.Lock;

public interface ConcurrentWikiGraph {

    /**
     * Returns a lock that grants mutual exclusion between threads that are computing the exact
     * same term at the same time. This is useful if you need to create nodes before edges, without
     * duplicate nodes, based on the terms and their children.
     * @param nodeTerm the term that is paired with the lock
     * @return the lock paired with input term
     */
    Lock getLockOn(final String nodeTerm);

    void setAborted();

    boolean isAborted();

    boolean contains(final String term);

    boolean addNode(final String term);

    void setRootID(final String term);

    String getRootID();
}
