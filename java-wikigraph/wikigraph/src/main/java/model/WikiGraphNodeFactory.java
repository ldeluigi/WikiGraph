package model;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * A factory that can generate nodes based on human queries.
 */
public interface WikiGraphNodeFactory {

    /**
     * Searches the web of similar terms in Wikipedia page titles.
     * Returns a {@link Map} with pairs of result - description (short).
     * Blocking behaviour.
     * Order of results should be of descending relevancy.
     *
     * @param term the search term
     * @return a list of results paired with short description
     */
    List<Pair<String, String>> search(final String term);

    /**
     * Creates a structural {@link WikiGraphNode} from a wikipedia page link like
     * "https://en.wikipedia.org/wiki/Albert_Einstein".
     * Blocking behaviour.
     *
     * @param url the wikipedia page URL
     * @return the node or null if something went wrong (term not found or connection problems)
     */
    WikiGraphNode from(URL url);

    /**
     * Creates a structural {@link WikiGraphNode} from a wikipedia page title like
     * "Albert Einstein" or "Albert_Einstein".
     * Can be used with {@link WikiGraphNode#childrenTerms()}.
     * Blocking behaviour.
     *
     * @param term the wikipedia page name
     * @return the node or null if something went wrong (term not found or connection problems)
     */
    WikiGraphNode from(String term);

    /**
     * Sets the internal language (the default) for this factory.
     * Checks the lang code in the Wikipedia commons database.
     * Blocking behaviour.
     *
     * @param langCode like "en", "it", "nap"
     * @return true if language is available and was set correctly
     */
    boolean setLanguage(String langCode);

    /**
     * Creates a structural {@link WikiGraphNode} from a random wikipedia page.
     * Blocking behaviour.
     *
     * @return he node or null if something went wrong
     */
    WikiGraphNode random();
}
