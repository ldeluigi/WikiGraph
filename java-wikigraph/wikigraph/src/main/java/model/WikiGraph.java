package model;

import java.util.Collection;
import java.util.Set;

public interface WikiGraph {
    Set<String> terms();
    Collection<WikiGraphNode> nodes();
    Set<Pair<String, String>> termEdges();
}
