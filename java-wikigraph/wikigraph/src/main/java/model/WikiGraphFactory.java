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

        private class PairImpl<K, V> implements Pair<K, V> {
            private final K key;
            private final V value;
            public PairImpl(K key, V value) {
                this.key = key;
                this.value = value;
            }

            public K getKey() {
                return key;
            }

            public V getValue() {
                return value;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof Pair)) return false;
                Pair<?, ?> pair = (Pair<?, ?>) o;
                return Objects.equals(getKey(), pair.getKey()) &&
                        Objects.equals(getValue(), pair.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hash(getKey(), getValue());
            }
        }

        @Override
        public Set<Pair<String, String>> termEdges() {
            return this.nodes.entrySet().stream()
                    .flatMap(e -> e.getValue()
                            .childrenTerms().stream()
                            .map(c -> new PairImpl(e.getKey(), c)))
                    .collect(Collectors.toUnmodifiableSet());
        }
    }
}
