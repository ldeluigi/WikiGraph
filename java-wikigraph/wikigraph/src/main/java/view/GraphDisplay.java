package view;

public interface GraphDisplay {
    void addNode(final String id, final int depth, final String lang);

    void addEdge(final String idFrom, final String idTo);

    void removeNode(final String id);

    void removeEdge(final String idFrom, final String idTo);

    void clearGraph();
}
