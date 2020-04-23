package model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class HttpWikiGraph implements WikiGraph {
    private static final String LANGUAGE_ENDPOINT =
            "https://commons.wikimedia.org/w/api.php?action=sitematrix&smtype=language&smsiteprop=url&format=json";
    private String locale = "en";

    private final String API_ENDPOINT() {
        return "https://" + this.locale + ".wikipedia.org/w/api.php";
    }

    @Override
    public List<GraphNode> search(String term) {
        return null;
    }

    @Override
    public GraphNode from(final URL url) {
        return null;
    }

    @Override
    public GraphNode from(final String term) {
        final String URLTerm = URLEncoder.encode(term.replace(" ", "_"),
                StandardCharsets.UTF_8);
        final HttpGET req = new HttpGET().setBaseURL(API_ENDPOINT())
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
            final List<String> sameTerm = new LinkedList<>();
            for (int i = 0; i < redirects.length(); i++) {
                sameTerm.add(redirects.getJSONObject(i).getString("from"));
            }
            final List<String> terms = new LinkedList<>();
            for (int i = 0; i < links.length(); i++) {
                final JSONObject linkObj = links.getJSONObject(i);
                if (linkObj.getInt("ns") == 0) {
                    final String linkTerm = linkObj.getString("*");
                    terms.add(linkTerm);
                }
            }
            return new HttpWikiGraphNode(this, termResult, new HashSet<>(sameTerm), new HashSet<>(terms));
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
}
