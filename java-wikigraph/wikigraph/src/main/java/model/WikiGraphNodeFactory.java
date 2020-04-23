package model;

import java.net.URL;
import java.util.Map;

public interface WikiGraphNodeFactory {
    Map<String, String> search(final String term);
    WikiGraphNode from(URL url);
    WikiGraphNode from(String term);
    boolean setLanguage(String langCode);

}
