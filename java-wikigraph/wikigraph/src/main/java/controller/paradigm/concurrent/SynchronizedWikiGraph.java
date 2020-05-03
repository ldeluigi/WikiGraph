package controller.paradigm.concurrent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implements: <br/>
 * - A container for the state "aborted" (yes/no) to abrupt computation <br/>
 * - A manager for {@link Lock}s that are separate for each term, to grant mutual exclusion between
 * tasks relative to the same term.
 */
public class SynchronizedWikiGraph implements ConcurrentWikiGraph {
    private final Map<String, Lock> locks = new HashMap<>();
    private AtomicBoolean aborted = new AtomicBoolean(false);
    private String root;
    private final Set<String> set = new HashSet<>();// TODO graphstream

    @Override
    public void setAborted() {
        this.aborted.set(true);
    }

    @Override
    public boolean isAborted() {
        return this.aborted.get();
    }

    @Override
    public boolean contains(final String term) {
        return this.set.contains(term);
    }

    @Override
    public boolean addNode(final String term) {
        return this.set.add(term);
    }

    @Override
    public void setRootID(String term) {
        this.root = term;
    }

    @Override
    public String getRootID() {
        return this.root;
    }

    /**
     * Returns an empty fresh new {@link SynchronizedWikiGraph}.
     *
     * @return a new empty synchronized graph
     */
    public static SynchronizedWikiGraph empty() {
        return new SynchronizedWikiGraph();
    }


    @Override
    public Lock getLockOn(final String nodeTerm) {
        synchronized (this.locks) {
            if (this.locks.containsKey(nodeTerm)) {
                return this.locks.get(nodeTerm);
            }
            final Lock newLock = new ReentrantLock();
            this.locks.put(nodeTerm, newLock);
            return newLock;
        }
    }
}
