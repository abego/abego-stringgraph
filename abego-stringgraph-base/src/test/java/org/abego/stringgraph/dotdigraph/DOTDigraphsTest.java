package org.abego.stringgraph.dotdigraph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DOTDigraphsTest {
    @Test
    void constructor() {
        assertThrows(UnsupportedOperationException.class,DOTDigraphs::new);
    }


    @Test
    void getDOTDigraphPrinter() {
        DOTDigraphPrinter printer = DOTDigraphs.getDOTDigraphPrinter();
        
        assertNotNull(printer);
    }
}
