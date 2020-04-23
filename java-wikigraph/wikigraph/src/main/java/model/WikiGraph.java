package model;

import java.net.URL;
import java.util.Map;

public interface WikiGraph {
    Map<String, String> search(final String term);
    GraphNode from(URL url);
    GraphNode from(String term);
    boolean setLanguage(String langCode);
}
