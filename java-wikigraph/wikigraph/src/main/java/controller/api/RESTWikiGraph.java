package controller.api;

import model.WikiGraphNode;
import model.WikiGraphNodeImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

public class RESTWikiGraph extends HttpWikiGraph {

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
            Document doc = Jsoup.connect(apiEndpoint(lang) + URLTerm).get();
            if (doc != null) {
                String termResult = doc.select("html head title").html();
                Elements links = doc.select("section:first-child p a:not([href*=#])");
                final Set<String> terms = new HashSet<>();
                links.forEach((Element link) -> {
                    if (link.attr("rel").equals("mw:WikiLink") && !link.attr("href").matches("\\w+:\\w")) {
                        terms.add(link.attr("title"));
                    }
                });
                return new WikiGraphNodeImpl(termResult, sameTerm, terms);
            }
        } catch (IOException e) {
            Logger logger = Logger.getLogger(RESTWikiGraph.class.getName());
            logger.warning("from "+ term +" ritorna null");
            return null;
        }
        System.err.println("RESTWikiGraph ritorna null senza eccezione");
        return null;

    }
}
