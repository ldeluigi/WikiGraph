package model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MutableGraphImpl implements MutableWikiGraph {
    private final Map<String, WikiGraphNode> nodeMap = new HashMap<>();
    private String root = null;
    private final WikiGraph view = WikiGraphs.from(this.nodeMap, null);

    @Override
    public boolean add(final WikiGraphNode node) {
        if (this.nodeMap.containsKey(node.term())) {
            return false;
        }
        this.nodeMap.put(node.term(), node);
        if (root == null) {
            root = node.term();
        }
        return true;
    }

    @Override
    public boolean remove(final String nodeTerm) {
        if (this.nodeMap.containsKey(nodeTerm)) {
            this.nodeMap.remove(nodeTerm);
            if (this.nodeMap.isEmpty()) {
                this.root = null;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean set(final WikiGraphNode node) {
        if (this.nodeMap.containsKey(node.term())) {
            this.nodeMap.put(node.term(), node);
            return true;
        }
        return false;
    }

    @Override
    public Set<String> terms() {
        return view.terms();
    }

    @Override
    public Collection<WikiGraphNode> nodes() {
        return view.nodes();
    }

    @Override
    public Set<Pair<String, String>> termEdges() {
        return view.termEdges();
    }

    @Override
    public boolean contains(final String term) {
        return view.contains(term);
    }

    @Override
    public String getRoot() {
        return this.root;
    }
}
