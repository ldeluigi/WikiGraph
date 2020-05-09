package model.api;

import com.google.common.util.concurrent.RateLimiter;
import model.WikiGraphNode;
import model.WikiGraphNodeImpl;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class RESTWikiGraph extends HttpWikiGraph {

    private final RateLimiter rateLimiter = RateLimiter.create(200);

    private String apiEndpoint(final String lang) {
        return "https://" + lang + ".wikipedia.org/api/rest_v1/page/html/";
    }

    @Override
    protected WikiGraphNode from(final String term, final String lang, final int depth) {
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
                Elements firstLanesLinks = doc.select("section:first-child>div>a:not([href*=#])");
                Elements links = doc.select("section:first-child p a:not([href*=#])");
                final Set<String> terms = new HashSet<>();
                this.addToSet(terms, firstLanesLinks);
                this.addToSet(terms, links);
                return new WikiGraphNodeImpl(termResult, depth, sameTerm, terms);
            }
            System.err.println("ERROR: RESTWikiGraph document is null");
        } catch (MalformedURLException e) {
            System.err.println("WARNING: " + term + ": malformed URL, " + e.getMessage());
        } catch (HttpStatusException e) {
            System.err.println("WARNING: " + term + ": http status " +
                    e.getStatusCode() + ", \n\t" + e.getUrl() + "\n\t" +
                    e.getMessage());
        } catch (UnsupportedMimeTypeException e) {
            System.err.println("WARNING: " + term + ": unsupported MIME " +
                    e.getMimeType() + ", " + e.getMessage());
        } catch (SocketTimeoutException e) {
            System.err.println("WARNING: " + term + ": socket timeout, " +
                    e.bytesTransferred + "B, " + e.getMessage());
        } catch (IOException e) {
            System.err.println("WARNING: " + term + ": " + e.getMessage());
        }
        return null;
    }

    private void addToSet(final Set<String> terms, final Elements links) {
        links.forEach((Element link) -> {
            if (link.attr("rel").equals("mw:WikiLink") && !link.attr("title").matches("\\w+:\\w.*")) {
                terms.add(link.attr("title"));
            }
        });
    }
}
