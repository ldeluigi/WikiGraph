package model;

public interface MutableWikiGraph extends WikiGraph {
    boolean add(final WikiGraphNode node);

    boolean remove(final String nodeTerm);

    boolean set(final WikiGraphNode node);
}
