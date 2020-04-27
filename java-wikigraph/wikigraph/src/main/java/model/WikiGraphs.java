package model;

import java.util.*;
import java.util.stream.Collectors;

public class WikiGraphs {

    public static WikiGraph from(Map<String, WikiGraphNode> nodes) {
        return new WikiGraph() {
            @Override
            public Set<String> terms() {
                final HashSet<String> all = new HashSet<>(nodes.keySet());
                nodes.values().stream().flatMap(v -> v.childrenTerms().stream()).collect(Collectors.toCollection(() -> all));
                return Collections.unmodifiableSet(all);
            }

            @Override
            public Collection<WikiGraphNode> nodes() {
                return Collections.unmodifiableCollection(nodes.values());
            }

            @Override
            public Set<Pair<String, String>> termEdges() {
                return nodes.entrySet().stream()
                        .flatMap(e -> e.getValue()
                                .childrenTerms().stream()
                                .map(c -> new PairImpl<>(e.getKey(), c)))
                        .collect(Collectors.toUnmodifiableSet());
            }

            @Override
            public boolean contains(final String term) {
                return nodes.containsKey(term);
            }
        };
    }
}
