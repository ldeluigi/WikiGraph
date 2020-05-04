package controller.paradigm.tasks;


import controller.Controller;
import controller.api.RESTWikiGraph;
import controller.paradigm.concurrent.ConcurrentWikiGraph;
import controller.paradigm.concurrent.SynchronizedWikiGraph;
import controller.update.GraphAutoUpdateRequest;
import controller.update.NoOpView;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;
import view.ViewEvent.EventType;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorController implements Controller {

    private final View view;
    private final Lock mutex = new ReentrantLock();
    private ScheduledThreadPoolExecutor pool;
    private Optional<ViewEvent> event = Optional.empty();
    private ConcurrentWikiGraph graphBeingComputed = null;
    private final AtomicReference<String> language = new AtomicReference<>(Locale.ENGLISH.getLanguage());
    private boolean isQuiescent = true;
    private boolean autoUpdate;
    private GraphAutoUpdateRequest autoUpdateReq;
    private boolean isUpdating;
    private int updateDelay;

    public ExecutorController(final View view) {
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start() {
        this.pool = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 5);
        this.view.start();
    }

    private void startComputing(final String root, final int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        this.pool.execute(() -> {
            nodeFactory.setLanguage(language.get());
            ConcurrentWikiGraph graph = SynchronizedWikiGraph.empty();
            mutex.lock();
            try {
                if (this.graphBeingComputed != null) {
                    System.out.println("START COMPUTING ABORTED LAST");
                    this.graphBeingComputed.setAborted();
                }
                this.graphBeingComputed = graph;
                this.isUpdating = false;
                this.autoUpdateReq = new GraphAutoUpdateRequest(nodeFactory, depth, graph);
            } finally {
                mutex.unlock();
            }
            new ComputeChildrenTask(nodeFactory, graph, this.view, depth, root) {
                @Override
                public void onCompletion(CountedCompleter<?> caller) {
                    super.onCompletion(caller);
                    mutex.lock();
                    try {
                        if (graphBeingComputed == graph){
                            System.out.println(graph.getRootID() + " IS LAST");
                            graphBeingComputed = null;
                            if (event.isPresent()) {
                                resolveEvent(event.get());
                                event = Optional.empty();
                            } else if (autoUpdate && !graph.isAborted()) {
                                System.out.println(graph.getRootID() + " STARTS AUTO UPDATING");
                                startAutoUpdating(graph.getRootID());
                            } else {
                                isQuiescent = true;
                            }
                        }
                    } finally {
                        mutex.unlock();
                    }
                }
            }.compute();
        });
    }

    private void exit() {
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }

    //u need to have lock to call this method
    private void resolveEvent(final ViewEvent event){
        switch (event.getType()) {
            case EXIT:
                this.exit();
                event.onComplete(true);
                break;
            case SEARCH:
                startComputing(event.getText(), event.getInt());
                event.onComplete(true);
                break;
            case RANDOM_SEARCH:
                startComputing(null, event.getInt());
                event.onComplete(true);
                break;
            case CLEAR:
                mutex.lock();
                try {
                    this.autoUpdateReq = null;
                } finally {
                    mutex.unlock();
                }
                this.view.clearGraph();
                this.isQuiescent = true;
                event.onComplete(true);
                break;
            case LANGUAGE:
                this.pool.execute(() -> {
                    if (new RESTWikiGraph().setLanguage(event.getText())) {
                        this.language.set(event.getText());
                        event.onComplete(true);
                    } else {
                        event.onComplete(false);
                    }
                });
                this.isQuiescent = true;
                break;
            case AUTO_UPDATE:
                mutex.lock();
                try {
                    this.isQuiescent = true;
                    if (event.getInt() < 0 && this.autoUpdate) {
                        this.autoUpdate = false;
                        System.out.println("OFF AUTO UPDATE");
                        if (this.graphBeingComputed != null && this.isUpdating) {
                            System.out.println("ABORTING LAST");
                            this.graphBeingComputed.setAborted();
                            this.graphBeingComputed = null;
                            this.isUpdating = false;
                        }
                    } else if (event.getInt() >= 0) {
                        this.updateDelay = event.getInt();
                        System.out.println("UPDATED DELAY");
                        this.autoUpdate = true;
                        System.out.println("ON AUTO UPDATE");
                        if (this.graphBeingComputed == null && this.autoUpdateReq != null) {
                            if (this.isUpdating) throw new IllegalStateException("is Updating left true");
                            System.out.println("STARTING AUTO UPDATE");
                            startAutoUpdating(this.autoUpdateReq.getOriginal().getRootID());
                        }
                    }
                } finally {
                    mutex.unlock();
                }
                event.onComplete(true);
                break;
        }
    }

    private void startAutoUpdating(final String forRoot) {
        System.out.println(forRoot+" has called startAutoUpdating");
        final ConcurrentWikiGraph graph = new SynchronizedWikiGraph();
        mutex.lock();
        try {
            final String root = this.autoUpdateReq.getOriginal().getRootID();
            if (this.autoUpdateReq == null || !this.autoUpdateReq.getOriginal().getRootID().equals(forRoot) || !this.autoUpdate) {
                System.out.println(forRoot + " UPDATE CANCELED");
                if (this.event.isPresent()) {
                    resolveEvent(event.get());
                    event = Optional.empty();
                }
                return;
            }
            this.isQuiescent = false;
            this.graphBeingComputed = graph;
            this.isUpdating = true;
            final WikiGraphNodeFactory nodeFactory = this.autoUpdateReq.getNodeFactory();
            final int depth = this.autoUpdateReq.getDepth();
            this.pool.execute(() -> new ComputeChildrenTask(nodeFactory, graph, new NoOpView(), depth, root) {
                @Override
                public void onCompletion(CountedCompleter<?> caller) {
                    super.onCompletion(caller);
                    mutex.lock();
                    try {
                        if (event.isPresent()) {
                            resolveEvent(event.get());
                            event = Optional.empty();
                        } else if (graphBeingComputed == graph && !graph.isAborted()) {
                            System.out.println("CALCULATING DIFFERENCES FOR " + root);
                            // HERE COMPUTE DIFFERENCES
                            graphBeingComputed = null;// ask for it, non sto computando niente o nessuno è l'ultimo?
                            if (autoUpdate) {
                                System.out.println("RESCHEDULING UPDATE FOR " + root);
                                autoUpdateReq.updateOriginal(graph);
                                pool.schedule(() -> startAutoUpdating(root), updateDelay, TimeUnit.MILLISECONDS);
                            } else {
                                System.out.println("SHUT OFF ");
                            }
                        } else {
                            System.out.println(root + "UPDATES QUIETLY SHUTDOWN");
                        }
                        isUpdating = false;
                        isQuiescent = true;
                    } finally {
                        mutex.unlock();
                    }
                }
            }.compute());
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public void notifyEvent(final ViewEvent event) {
        this.mutex.lock();
        try {
            this.abortIfEventNeeds(event);
            if (this.isQuiescent) {
                this.isQuiescent = false;
                this.resolveEvent(event);
            } else {
                this.event = Optional.of(event);
            }
        } finally {
            this.mutex.unlock();
        }
    }

    private void abortIfEventNeeds(final ViewEvent event) {
        List<EventType> events = Arrays.asList(EventType.RANDOM_SEARCH, EventType.SEARCH, EventType.LANGUAGE, EventType.CLEAR);
        System.out.println(event.getType());
        if (this.graphBeingComputed != null && events.contains(event.getType())) {
            this.graphBeingComputed.setAborted();
        }
    }

}
