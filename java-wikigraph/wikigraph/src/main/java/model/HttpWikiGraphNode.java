package model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class HttpWikiGraphNode implements GraphNode {
    private final String name;
    private final Set<String> synonyms;
    private final Set<String> children;

    public HttpWikiGraphNode(final String term, Set<String> children) {
        this(term, Collections.emptySet(), children);
    }

    public HttpWikiGraphNode(final String term, Set<String> synonyms, Set<String> children) {
        this.name = term;
        this.synonyms = synonyms;
        this.children = children;
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
    public boolean equals(GraphNode other) {
        if (other == null) return false;
        if (other.term().equals(this.term())) return true;
        if (this.synonyms.contains(other.term())) return true;
        return false;
    }

    @Override
    public String toString() {
        return "HttpWikiGraphNode{" +
                "name='" + name + '\'' +
                ", synonyms=" + synonyms +
                ", children=" + children +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof GraphNode)) return false;
        GraphNode that = (GraphNode) o;
        return this.equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, synonyms, children);
    }
}
