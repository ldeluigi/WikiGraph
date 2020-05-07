package controller.paradigm.reactivex;

import controller.utils.AbortedOperationException;
import controller.utils.WikiGraphManager;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.schedulers.Schedulers;
import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

public class RecursiveGraphOperation extends Observable<WikiGraphManager> {
    private final Executor executor;

    private final WikiGraphManager graph;
    private final WikiGraphNodeFactory factory;
    private final int maxDepth;
    private final int depth;
    private String id;
    private final String fatherID;
    private final Observable<WikiGraphManager> obs;

    private RecursiveGraphOperation(final RecursiveGraphOperation father,
                                    final String term) {
        this.graph = father.graph;
        this.factory = father.factory;
        this.maxDepth = father.maxDepth;
        this.depth = father.depth + 1;
        this.fatherID = father.id;
        this.executor = father.executor;
        this.obs = setupRecursiveRxAlgorithm(term);
    }

    public RecursiveGraphOperation(final WikiGraphNodeFactory factory,
                                   final int maxDepth,
                                   final String root,
                                   final WikiGraphManager graph,
                                   final Executor executor) {
        this.factory = factory;
        this.maxDepth = maxDepth;
        this.graph = graph;
        this.depth = 0;
        this.fatherID = null;
        this.executor = executor;
        this.obs = setupRecursiveRxAlgorithm(root);
    }

    @Override
    protected void subscribeActual(final @NonNull Observer<? super WikiGraphManager> observer) {
        this.obs.subscribe(observer);
    }

    private Observable<WikiGraphManager> setupRecursiveRxAlgorithm(final String root) {
        return Observable.fromCallable(() -> Optional.ofNullable(root))
                .observeOn(Schedulers.from(this.executor))
                .map(term -> {
                    if (this.graph.isAborted()) {
                        throw new AbortedOperationException();
                    }
                    final WikiGraphNode result;
                    if (this.depth == 0) {
                        System.out.println("RX START: " + root);
                        if (term.isEmpty()) { //random
                            result = this.factory.random(0);
                        } else { //search
                            WikiGraphNode temp;
                            try {
                                temp = this.factory.from(new URL(term.get()),depth);
                            } catch (MalformedURLException | IllegalArgumentException e) {
                                temp = this.factory.from(term.get(), 0);
                                if (temp == null) {
                                    final List<Pair<String, String>> closest = this.factory.search(term.get());
                                    if (closest.size() > 0) {
                                        temp = this.factory.from(closest.get(0).getKey(), 0);
                                    }
                                }
                            }
                            result = temp;
                        }
                        if (result != null) {
                            this.graph.setRootID(result.term());
                        }
                    } else {
                        result = this.factory.from(term.get(), this.depth);
                    }
                    return Optional.ofNullable(result);
                })
                .observeOn(Schedulers.single())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .doOnNext(node -> this.id = node.term())
                .flatMap(node -> {
                    if (this.graph.contains(node.term())) {
                        this.graph.addEdge(this.fatherID, node.term());
                    } else {
                        this.graph.addNode(node.term(), depth, this.factory.getLanguage());
                        if (this.depth > 0) {
                            this.graph.addEdge(this.fatherID, node.term());
                        }
                        if (this.depth < this.maxDepth) {
                            return Observable.fromIterable(node.childrenTerms())
                                    .flatMap(term -> new RecursiveGraphOperation(this, term));
                        }
                    }
                    return Observable.just(node.term());
                })
                .takeLast(1)
                .map(s -> this.graph);
    }
}
