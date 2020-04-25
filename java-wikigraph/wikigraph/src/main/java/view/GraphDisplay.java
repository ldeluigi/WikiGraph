package view;

interface GraphDisplay {
    void addNode(String id);
    void addEdge(String idFrom, String idTo);
    void removeNode(String id);
    void removeEdge(String idFrom, String idTo);
}
