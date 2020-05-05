package model;

import java.io.IOException;
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
     * @param url   the wikipedia page URL
     * @param depth the depth of the node
     * @return the node or null if something went wrong (term not found or connection problems)
     */
    WikiGraphNode from(final URL url, final int depth);

    /**
     * Creates a structural {@link WikiGraphNode} from a wikipedia page title like
     * "Albert Einstein" or "Albert_Einstein".
     * Can be used with {@link WikiGraphNode#childrenTerms()}.
     * Blocking behaviour.
     *
     * @param term  the wikipedia page name
     * @param depth the depth of the node
     * @return the node or null if something went wrong (term not found or connection problems)
     */
    WikiGraphNode from(final String term, final int depth);

    /**
     * Sets the internal language (the default) for this factory.
     * Checks the lang code in the Wikipedia commons database.
     * Blocking behaviour.
     *
     * @param langCode like "en", "it", "nap"
     * @return true if language is available and was set correctly
     */
    boolean setLanguage(final String langCode) throws IOException;

    /**
     * Creates a structural {@link WikiGraphNode} from a random wikipedia page.
     * Blocking behaviour.
     *
     * @param depth the depth of the node
     * @return he node or null if something went wrong
     */
    WikiGraphNode random(final int depth);

    /**
     * Returns the internal (default) language for this factory.
     *
     * @return the language code
     */
    String getLanguage();

    /**
     * Returns the language to which the url refers.
     *
     * @param url a wikipedia url
     * @return the language code
     */
    String getLanguage(final URL url);
}
