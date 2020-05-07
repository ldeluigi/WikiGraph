package view;

import org.graphstream.stream.SourceBase;

import java.lang.annotation.ElementType;

public interface GraphDisplay {
    void addNode(final String id, final int depth, final String lang);

    void updateDepthNode(String id, int depth);

    void addEdge(final String idFrom, final String idTo);

    void removeNode(final String id);

    void removeEdge(final String idFrom, final String idTo);

    void clearGraph();
}
