package controller.api;

import model.WikiGraphNode;
import model.WikiGraphNodeImpl;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RESTWikiGraph extends HttpWikiGraph {

    private String apiEndpoint(final String lang) {
        return "https://" + lang + ".wikipedia.org/api/rest_v1/page/html/";
    }

    @Override
    protected WikiGraphNode from(final String term, final String lang) {
        Set<String> sameTerm = new HashSet<>();
        sameTerm.add(term);
        final String URLTerm = term.replace(" ", "_");
        try {
            Document doc = Jsoup.connect(apiEndpoint(lang) + URLTerm).get();
            if (doc != null) {
                String termResult = doc.select("html head title").html();
                Elements links = doc.select("section:first-child p a:not([href*=#])");
                final Set<String> terms = new HashSet<>();
                links.forEach((Element link) -> terms.add(link.attr("title")));
                return new WikiGraphNodeImpl(termResult, sameTerm, terms);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.err.println("RESTWikiGraph ritorna null");
        return null;

    }
}
