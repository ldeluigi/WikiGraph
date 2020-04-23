package model;

import java.util.*;
import java.util.stream.Collectors;

public class WikiGraphFactory {

    public static WikiGraph from(Map<String, WikiGraphNode> nodes) {
        return new WikiGraphMap(nodes);
    }

    public static WikiGraph from(Collection<WikiGraphNode> nodes) {
        return from(nodes.stream().collect(Collectors.toUnmodifiableMap(WikiGraphNode::term, n -> n)));
    }

    public static WikiGraph from(WikiGraphNode... nodes) {
        return from(Arrays.asList(nodes));
    }

    private static class WikiGraphMap implements WikiGraph {
        private final Map<String, WikiGraphNode> nodes;

        public WikiGraphMap(Map<String, WikiGraphNode> nodes) {
            this.nodes = Collections.unmodifiableMap(nodes);
        }

        @Override
        public Set<String> terms() {
            return this.nodes.keySet();
        }

        @Override
        public Collection<WikiGraphNode> nodes() {
            return Collections.unmodifiableCollection(this.nodes.values());
        }

        @Override
        public Set<Pair<String, String>> termEdges() {
            return this.nodes.entrySet().stream()
                    .flatMap(e -> e.getValue()
                            .childrenTerms().stream()
                            .map(c -> new PairImpl<>(e.getKey(), c)))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }
}
