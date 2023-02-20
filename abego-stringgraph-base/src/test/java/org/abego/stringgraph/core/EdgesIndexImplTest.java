package org.abego.stringgraph.core;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

class EdgesIndexImplTest {
    @Test
    void smoketest() {
        EdgesIndex<Integer> index = EdgesIndexImpl.createEdgesIndex(emptyMap());

        assertEquals(0, index.edgesCount(1));
    }
}
