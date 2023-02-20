package org.abego.stringgraph.core;

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
    }
}
