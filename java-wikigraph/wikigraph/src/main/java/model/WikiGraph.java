package model;

import java.net.URL;
import java.util.List;

public interface WikiGraph {
    List<GraphNode> search(final String term);
    GraphNode from(URL url);
    GraphNode from(String term);
    boolean setLanguage(String langCode);
}
