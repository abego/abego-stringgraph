package org.abego.stringgraph.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NodeTest {
    public static void assertNodeEquals(String expectedNodeId, Node actual) {
        assertEquals(expectedNodeId, actual.id());
    }
}
