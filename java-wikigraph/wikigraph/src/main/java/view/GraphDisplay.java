package view;

interface GraphDisplay {
    void newNode(String id);
    void newEdge(String idFrom, String idTo);
    void removeNode(String id);
    void removeEdge(String idFrom, String idTo);
}
