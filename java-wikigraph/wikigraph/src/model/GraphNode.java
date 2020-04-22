package model;

import java.util.Map;

public interface GraphNode {
    Map<GraphNode, Integer> children();
}
