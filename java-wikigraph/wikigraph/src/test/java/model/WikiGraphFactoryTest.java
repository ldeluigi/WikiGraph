package model;

import controller.api.HttpWikiGraph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.util.*;

class WikiGraphFactoryTest {

    @Test
    void from() {
        final Set<WikiGraphNode> a = new HashSet<>(Arrays.asList(new HttpWikiGraph().from("UK"), new HttpWikiGraph().from("Albert Einstein")));
        final Set<String> all = new HashSet<>();
        for (WikiGraphNode n : a) {
            all.addAll(n.childrenTerms());
            all.add(n.term());
        }
        final Set<Pair<String, String>> edges = new HashSet<>();
        for (WikiGraphNode n : a) {
            for (String c : n.childrenTerms()) {
                edges.add(new PairImpl<>(n.term(), c));
            }
        }
        final WikiGraph g = WikiGraphs.from(a);
        assertEquals(a, new HashSet<>(g.nodes()));
        assertEquals(all, g.terms());
        assertEquals(edges, g.termEdges());
    }
}