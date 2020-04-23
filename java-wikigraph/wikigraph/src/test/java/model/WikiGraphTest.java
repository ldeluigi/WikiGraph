package model;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class WikiGraphTest {

    @Test
    void testSetLanguage() {
        final WikiGraph g = new HttpWikiGraph();
        assertTrue(g.setLanguage("en"));
        assertTrue(g.setLanguage("it"));
        assertFalse(g.setLanguage("???"));
    }

    @Test
    void testFrom() {
        final WikiGraph g = new HttpWikiGraph();
        assertNull(g.from(""));
        assertNotNull(g.from("UK"));
        assertNotNull(g.from("Albert Einstein"));
        assertEquals("United Kingdom", g.from("UK").term());
        assertTrue(g.from("Albert Einstein").childrenTerms().size() > 10);
    }

    @Test
    void testFromURL() {
        final WikiGraph g = new HttpWikiGraph();
        try {
            assertNotNull(g.from(new URL("https://en.wikipedia.org/wiki/Introduction_to_quantum_mechanics#Wave%E2%80%93particle_duality")));
            assertNull(g.from(new URL("https://en.wikipedia.org/wiki/Test_dummy#42")));
            assertTrue(g.from(new URL("https://en.wikipedia.org/wiki/Introduction_to_quantum_mechanics#Wave%E2%80%93particle_duality")).childrenTerms().size() > 10);
            assertEquals("United Kingdom", g.from(new URL("https://en.wikipedia.org/wiki/UK#42")).term());
        } catch (MalformedURLException e) {
            fail(e);
        }
    }

    @Test
    void testSearch() {
        final WikiGraph g = new HttpWikiGraph();
        assertEquals(0, g.search("asigkadgshjs").size());
        assertEquals(HttpWikiGraph.SEARCH_RESULT_SIZE, g.search("Uk").size());
        System.out.println( g.search("Uk"));
    }
}
