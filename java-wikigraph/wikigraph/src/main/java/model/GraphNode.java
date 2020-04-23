package model;

import java.util.Set;

public interface GraphNode {
    Set<String> childrenTerms();
    String term();
}
