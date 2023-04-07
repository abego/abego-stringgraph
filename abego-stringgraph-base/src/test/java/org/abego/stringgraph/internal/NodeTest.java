/*
 * MIT License
 *
 * Copyright (c) 2023 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.internal;

import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.StringGraph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.abego.stringgraph.core.StringGraphTest.getSampleABCDEF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NodeTest {
    public static void assertNodeEquals(String expectedNodeId, Node actual) {
        assertEquals(expectedNodeId, actual.id());
    }

    @Test
    void asNodeImpl() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> NodeImpl.asNodeImpl(null));
        assertEquals("NodeImpl expected, got null", e.getMessage());
    }

    @Test
    void equals() {
        StringGraph graph = getSampleABCDEF();

        List<Node> twoNodesList = graph.nodes("A", null, "?").stream()
                .collect(Collectors.toList());
        Node n1 = twoNodesList.get(0);
        Node n2 = twoNodesList.get(1);
        assertEquals(n1, n1);
        assertNotEquals(n1, null);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(n1, "no Node");
        assertNotEquals(n1, n2);
    }
}
