package model;

import java.util.Objects;

/**
 * Implementation of the {@link Pair} interface
 *
 * @param <K> left element type
 * @param <V> right element type
 */
public class PairImpl<K, V> implements Pair<K, V> {
    private final K key;
    private final V value;

    /**
     * Basic constructor that takes two parameters.
     *
     * @param key   left element
     * @param value right element
     */
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

    @Override
    public String toString() {
        return "(" +
                "" + key +
                ", " + value +
                ')';
    }
}
