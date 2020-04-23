package model;


import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WikiGraphTest {
    private WikiGraph g;

    @BeforeEach
    void setWikiGraph() {
        this.g = new HttpWikiGraph();
    }

    @Test
    void testSetLanguage() {
        assertTrue(this.g.setLanguage("en"));
        assertTrue(this.g.setLanguage("it"));
        assertFalse(this.g.setLanguage("???"));
    }

    @Test
    void testFrom() {
        assertNull(this.g.from(""));
        assertNotNull(this.g.from("UK"));
        assertNotNull(this.g.from("Albert Einstein"));
        assertEquals("United Kingdom", this.g.from("UK").term());
        assertTrue(this.g.from("Albert Einstein").childrenTerms().size() > 10);
    }
}
