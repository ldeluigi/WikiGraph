package model.api;


import static org.junit.jupiter.api.Assertions.*;

import model.WikiGraphNodeFactory;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpWikiGraphTest {

    @Test
    void testSetLanguage() throws IOException {
        final WikiGraphNodeFactory g = new HttpWikiGraph();
        assertTrue(g.setLanguage("en"));
        assertTrue(g.setLanguage("it"));
        assertFalse(g.setLanguage("???"));
    }

    @Test
    void testFrom() {
        final WikiGraphNodeFactory g = new HttpWikiGraph();
        assertNull(g.from("", 0));
        assertNotNull(g.from("UK", 0));
        assertNotNull(g.from("Albert Einstein", 0));
        assertEquals("United Kingdom", g.from("UK", 0).term());
        assertTrue(g.from("Albert Einstein", 0).childrenTerms().size() > 10);
    }

    @Test
    void testFromURL() {
        final WikiGraphNodeFactory g = new HttpWikiGraph();
        try {
            assertNotNull(g.from(new URL("https://en.wikipedia.org/wiki/Introduction_to_quantum_mechanics#Wave%E2%80%93particle_duality"), 0));
            assertNull(g.from(new URL("https://en.wikipedia.org/wiki/Test_dummy#42"), 0));
            assertTrue(g.from(new URL("https://en.wikipedia.org/wiki/Introduction_to_quantum_mechanics#Wave%E2%80%93particle_duality"), 0).childrenTerms().size() > 10);
            assertEquals("United Kingdom", g.from(new URL("https://en.wikipedia.org/wiki/UK#42"), 0).term());
        } catch (MalformedURLException e) {
            fail(e);
        }
    }

    @Test
    void testSearch() {
        final WikiGraphNodeFactory g = new HttpWikiGraph();
        assertEquals(0, g.search("asigkadgshjs").size());
        assertEquals(HttpWikiGraph.SEARCH_RESULT_SIZE, g.search("Uk").size());
        assertEquals("United Kingdom", g.search("UK").get(0).getKey());
    }

    @Test
    void testSynonyms() {
        final WikiGraphNodeFactory g = new HttpWikiGraph();
        assertEquals(g.from("UK", 0), g.from("United Kingdom", 0));
    }

    @Test
    void testRandom() {
        final WikiGraphNodeFactory g = new HttpWikiGraph();
        assertNotNull(g.random(0));
    }
}
