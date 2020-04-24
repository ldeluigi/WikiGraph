package model;

/**
 * Interface for a pair.
 *
 * @param <K> Type of left object
 * @param <V> Type of right object
 */
public interface Pair<K, V> {
    /**
     * Returns the left element.
     *
     * @return left element of type K
     */
    K getKey();

    /**
     * Returns the right element.
     *
     * @return right element of type V
     */
    V getValue();
}
