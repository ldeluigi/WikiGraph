package controller.api;

import model.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class HttpWikiGraph implements WikiGraphNodeFactory {
    public static final int SEARCH_RESULT_SIZE = 10;
    private static final String LANGUAGE_ENDPOINT =
            "https://commons.wikimedia.org/w/api.php?action=sitematrix&smtype=language&smsiteprop=url&format=json";
    private String locale = "en";
    private String apiEndpoint() {
        return apiEndpoint(this.locale);
    }

    private String apiEndpoint(final String lang) {
        return "https://" + lang + ".wikipedia.org/w/api.php";
    }

    @Override
    public List<Pair<String, String>> search(String term) {
        final String URLTerm = term.replace(" ", "_");
        final HttpGET req = new HttpGET().setBaseURL(apiEndpoint())
                .addParameter("format", "json")
                .addParameter("action", "query")
                .addParameter("list", "search")
                .addParameter("srnamespace", "0")
                .addParameter("srprop", "snippet")
                .addParameter("srsearch", "intitle:" + URLTerm)
                .addParameter("srlimit", Integer.toString(SEARCH_RESULT_SIZE));
        final String rawJSON = req.send();
        final JSONObject result = new JSONObject(rawJSON);
        if (result.has("query")) {
            final JSONObject query = result.getJSONObject("query");
            final JSONArray searchResults = query.getJSONArray("search");
            final List<Pair<String, String>> finalResults = new LinkedList<>();
            for (int i = 0; i < searchResults.length(); i++) {
                final JSONObject resultObj = searchResults.getJSONObject(i);
                finalResults.add(new PairImpl<>(resultObj.getString("title"),
                        resultObj.getString("snippet")
                                .replaceAll("<.*?>|\\(.*?\\)|\\[.*?]|&.+?;", "")));
            }
            return finalResults;
        }
        throw new IllegalStateException("Wrong JSON: " + rawJSON);
    }

    @Override
    public WikiGraphNode from(final URL url) {
        final Pattern pattern = Pattern.compile(".+wiki/([^/#]+).*");
        final Matcher matcher = pattern.matcher(url.getPath());
        if (matcher.find()) {
            return this.from(matcher.group(1), this.getLanguage(url));
        }
        throw new IllegalArgumentException("url didn't match patterns");
    }

    @Override
    public WikiGraphNode from(final String term) {
        return this.from(term, this.locale);
    }

    protected WikiGraphNode from(final String term, final String lang) {
        final String URLTerm = term.replace(" ", "_");
        final HttpGET req = new HttpGET().setBaseURL(apiEndpoint(lang))
                .addParameter("format", "json")
                .addParameter("action", "parse")
                .addParameter("page", URLTerm)
                .addParameter("section", "0")
                .addParameter("prop", "links")
                .addParameter("redirects", "1")
                .addParameter("noimages", "1")
                .addParameter("disabletoc", "1");
        final String rawJSON = req.send();
        final JSONObject result = new JSONObject(rawJSON);
        if (result.has("parse")) {
            final JSONObject json = result.getJSONObject("parse");
            final JSONArray redirects = json.getJSONArray("redirects");
            final JSONArray links = json.getJSONArray("links");

            final String termResult = json.getString("title");
            final Set<String> sameTerm = new HashSet<>();
            for (int i = 0; i < redirects.length(); i++) {
                sameTerm.add(redirects.getJSONObject(i).getString("from"));
            }
            final Set<String> terms = new HashSet<>();
            for (int i = 0; i < links.length(); i++) {
                final JSONObject linkObj = links.getJSONObject(i);
                if (linkObj.getInt("ns") == 0) {
                    final String linkTerm = linkObj.getString("*");
                    terms.add(linkTerm);
                }
            }
            return new WikiGraphNodeImpl(termResult, sameTerm, terms);
        } else {
            return null;
        }
    }

    @Override
    public boolean setLanguage(final String langCode) {
        Objects.requireNonNull(langCode);
        final String rawJSON = new HttpGET()
                .setBaseURL(LANGUAGE_ENDPOINT).send();
        final JSONObject json = new JSONObject(rawJSON).getJSONObject("sitematrix");
        final boolean ok = IntStream.iterate(0, i -> i + 1).boxed()
                .takeWhile(i -> json.has(i.toString()))
                .map(i -> json.getJSONObject(i.toString()))
                .anyMatch(lan -> lan.has("code") && lan.getString("code")
                        .equals(langCode));
        if (ok) {
            this.locale = langCode;
            return true;
        }
        return false;
    }

    @Override
    public WikiGraphNode random() {
        final HttpGET req = new HttpGET().setBaseURL(apiEndpoint())
                .addParameter("format", "json")
                .addParameter("action", "query")
                .addParameter("list", "random")
                .addParameter("rnnamespace", "0");
        final String out = req.send();
        final JSONObject obj = new JSONObject(out);
        if (obj.has("query")) {
            final JSONObject queryResult = obj.getJSONObject("query");
            if (queryResult.has("random")) {
                final JSONArray results = queryResult.getJSONArray("random");
                if (results.length() > 0) {
                    final JSONObject result =  results.getJSONObject(0);
                    if (result.has("title")) {
                        return from(result.getString("title"));
                    }
                }
            }
        }
        throw new IllegalStateException("Wrong JSON: " + out);
    }

    @Override
    public String getLanguage() {
        return this.locale;
    }

    @Override
    public String getLanguage(final URL url) {
        final Pattern langPattern = Pattern.compile(".*?(\\w+)\\.wikipedia\\..+");
        final Matcher langMatch = langPattern.matcher(url.getHost());
        if (langMatch.find()) {
            return langMatch.group(1);
        }
        return this.locale;
    }
}
