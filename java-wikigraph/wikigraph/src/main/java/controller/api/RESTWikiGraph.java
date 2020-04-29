package controller.api;

import com.google.common.util.concurrent.RateLimiter;
import model.WikiGraphNode;
import model.WikiGraphNodeImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class RESTWikiGraph extends HttpWikiGraph {

    private RateLimiter rateLimiter = RateLimiter.create(200);

    private String apiEndpoint(final String lang) {
        return "https://" + lang + ".wikipedia.org/api/rest_v1/page/html/";
    }

    @Override
    protected WikiGraphNode from(final String term, final String lang) {
        Set<String> sameTerm = new HashSet<>();
        sameTerm.add(term);
        String URLTerm = term.replace(" ", "_");
        try {
            URLTerm = URLEncoder.encode(URLTerm, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            rateLimiter.acquire();
            Document doc = Jsoup.connect(apiEndpoint(lang) + URLTerm).get();
            if (doc != null) {
                String termResult = doc.select("html head title").html();
                Elements firstLanesLinks = doc.select("section:first-child>div:first-child>a:not([href*=#])");
                Elements links = doc.select("section:first-child p a:not([href*=#])");
                final Set<String> terms = new HashSet<>();
                this.addToSet(terms, firstLanesLinks);
                this.addToSet(terms, links);
                return new WikiGraphNodeImpl(termResult, sameTerm, terms);
            }
        } catch (IOException e) {
            System.err.println("WARNING: "+term +" is returning null");
            return null;
        }
        System.err.println("ERROR: RESTWikiGraph is returning null without exception");
        return null;
    }

    private void addToSet(Set<String> terms, Elements links){
        links.forEach((Element link) -> {
            if (link.attr("rel").equals("mw:WikiLink") && !link.attr("title").matches("\\w+:\\w.*")) {
                terms.add(link.attr("title"));
            }
        });
    }
}
