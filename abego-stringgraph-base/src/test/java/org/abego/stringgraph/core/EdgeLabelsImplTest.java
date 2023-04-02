package org.abego.stringgraph.core;

import org.abego.stringgraph.internal.EdgeLabelsImpl;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class EdgeLabelsImplTest {
    @Test
    void smoketest() {
        EdgeLabels el = EdgeLabelsImpl.createEdgeLabels(Collections.emptySet());

        assertEquals(0, el.getSize());
        assertNotNull(el.stream());
        assertEquals(el, el);
        assertNotEquals(el, null);
        assertNotEquals(el, "");
        assertEquals(el, EdgeLabelsImpl.createEdgeLabels(Collections.emptySet()));
        
        //The following checks an implementation detail. 
        // If implementation changes, the assert may need a change, too.
        assertEquals(31, el.hashCode());
    }
}
