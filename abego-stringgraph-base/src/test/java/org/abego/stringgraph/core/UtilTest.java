package org.abego.stringgraph.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {

    @Test
    void constructor() {
        assertThrows(UnsupportedOperationException.class, Util::new);
    }
    
    @Test
    void quotedIfNeeded() {
        assertEquals("\"\"",Util.quotedIfNeeded(""));
        assertEquals("a",Util.quotedIfNeeded("a"));
        assertEquals("abc",Util.quotedIfNeeded("abc"));
        assertEquals("-aAzZ09_+*=.:;/@?&#()[]{}<>",
                Util.quotedIfNeeded("-aAzZ09_+*=.:;/@?&#()[]{}<>"));
        assertEquals("\"two words\"",Util.quotedIfNeeded("two words"));
        assertEquals("\"two\\nlines\"",Util.quotedIfNeeded("two\nlines"));
        assertEquals("\"Ü=Umlaut-U\"",Util.quotedIfNeeded("Ü=Umlaut-U"));
    }
}
