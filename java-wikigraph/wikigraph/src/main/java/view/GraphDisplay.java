package view;

interface GraphDisplay {
    void addNode(String id, final int depth, final String lang);

    void addEdge(String idFrom, String idTo);

    void removeNode(String id);

    void removeEdge(String idFrom, String idTo);

    void clearGraph();
}
