package model;

import java.util.Collections;
import java.util.Set;

public class HttpWikiGraphNode implements GraphNode {
    private final String name;
    private final Set<String> synonyms;
    private final Set<String> children;
    private final WikiGraph graph;

    public HttpWikiGraphNode(final WikiGraph graph, final String term, Set<String> children) {
        this(graph, term, Collections.emptySet(), children);
    }

    public HttpWikiGraphNode(final WikiGraph graph, final String term, Set<String> synonyms, Set<String> children) {
        this.name = term;
        this.synonyms = synonyms;
        this.children = children;
        this.graph = graph;
    }

    @Override
    public Set<String> childrenTerms() {
        return this.children;
    }

    @Override
    public String term() {
        return this.name;
    }

    @Override
    public String toString() {
        return "HttpWikiGraphNode{" +
                "name='" + name + '\'' +
                ", synonyms=" + synonyms +
                ", children=" + children +
                '}';
    }
}
