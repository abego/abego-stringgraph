/*
 * MIT License
 *
 * Copyright (c) 2022 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.core;

import org.abego.commons.lang.IterableUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringGraphTest {
    private static final StringGraph SAMPLE1 =
            constructSample1(StringGraphBuilderImpl.createStringGraphBuilder())
                    .build();

    static Stream<StringGraph> stringGraphSample1Provider() {
        return stringGraphBuilderProvider()
                .map(b -> constructSample1(b).build());
    }

    static Stream<StringGraphBuilder> stringGraphBuilderProvider() {
        return StringGraphBuilderTest.stringGraphBuilderProvider();
    }

    public static void assertNodesAsLinesEquals(
            String expectedStringsInLines, Iterable<Node> nodes) {
        assertEquals(expectedStringsInLines,
                IterableUtil.size(nodes) + "\n" +
                        IterableUtil.asSortedLines(nodes, Node::id));
    }

    public static void assertStringsAsLinesEquals(
            String expectedStringsInLines, Iterable<String> strings) {
        assertEquals(expectedStringsInLines,
                IterableUtil.size(strings) + "\n" +
                        IterableUtil.asSortedLines(strings));
    }

    public static void assertNodesEquals(
            String expectedStringsInLines, Nodes nodes) {
        assertNodesAsLinesEquals(expectedStringsInLines, nodes);
    }

    /**
     * Returns a "Sample1" graph, created by any available StringGraphBuilder.
     * <p>
     * The StringGraphBuilder used may change any time.
     */
    public static StringGraph getSample1() {
        return SAMPLE1;
    }

    public static StringGraph getSingleEdgeGraph() {
        StringGraphBuilder builder = StringGraphs.createStringGraphBuilder();
        builder.addEdge("from", "to", "label");
        return builder.build();
    }

    public static <T extends StringGraphConstructing> T constructSample1(T constructing) {
        constructing.addNode("a");
        constructing.addNode("b");
        constructing.addNode("c"); // will later become a cycle
        constructing.addEdge("d", "e");
        constructing.addEdge("f", "g", "h");
        constructing.addEdge("i", "i", "cycle");
        constructing.addEdge("c", "c", "cycle");
        constructing.addEdge("o", "m1", "field");
        constructing.addEdge("o", "m2", "field");
        constructing.addEdge("o", "m3", "");
        constructing.setNodeProperty("a", "prop1", "");
        constructing.setNodeProperty("a", "prop2", "foo");
        return constructing;
    }

    public static void assertEqualsToAllNodesOfSample1(Nodes nodes) {
        assertNodesEquals("12\n" +
                        "a\n" +
                        "b\n" +
                        "c\n" +
                        "d\n" +
                        "e\n" +
                        "f\n" +
                        "g\n" +
                        "i\n" +
                        "m1\n" +
                        "m2\n" +
                        "m3\n" +
                        "o",
                nodes);
    }

    public static void assertEqualsToAllEdgesOfSample1(Edges edges) {
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("7\n" +
                        "\"c\" -> \"c\" : \"cycle\"\n" +
                        "\"d\" -> \"e\"\n" +
                        "\"f\" -> \"g\" : \"h\"\n" +
                        "\"i\" -> \"i\" : \"cycle\"\n" +
                        "\"o\" -> \"m3\"\n" +
                        "\"o\" -> \"m1\" : \"field\"\n" +
                        "\"o\" -> \"m2\" : \"field\"",
                edges);
    }

    public static void assertEqualsToAllNodePropsOfSample1(StringGraph graph) {
        graph.nodes().forEach(node -> {
            Properties props = graph.getProperties(node);
            if (node.id().equals("a")) {
                assertEquals(2, props.getSize());
                assertEquals("", props.getValueOfProperty("prop1"));
                assertEquals("foo", props.getValueOfProperty("prop2"));
            } else {
                assertEquals(0, props.getSize());
            }
        });
    }

    public static void assertEqualToSample1(StringGraph graph) {
        assertEqualsToAllNodesOfSample1(graph.nodes());
        assertEqualsToAllEdgesOfSample1(graph.edges());
        assertEqualsToAllNodePropsOfSample1(graph);
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void hasEdge(StringGraph sample1) {
        assertTrue(sample1.hasEdge("o", "m1", "field"));
        assertFalse(sample1.hasEdge("x", "y", "z"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void nodes(StringGraph sample1) {
        assertEqualsToAllNodesOfSample1(sample1.nodes());
    }

    @Test
    void nodesWithPattern() {

        StringGraph graph = getSampleABCDEF();

        assertNodesEquals("3\nA\nC\nE",
                graph.nodes("?", null, null));
        assertNodesEquals("3\nB\nD\nF",
                graph.nodes(null, null, "?"));
        assertNodesEquals("6\nA\nB\nC\nD\nE\nF",
                graph.nodes("?", null, "?"));
        assertNodesEquals("2\nA\nC",
                graph.nodes("?", null, "B"));
        assertNodesEquals("2\nB\nD",
                graph.nodes("A", null, "?"));
        assertNodesEquals("1\nA",
                graph.nodes("?", "e1", "B"));
        assertNodesEquals("1\nD",
                graph.nodes("A", "e2", "?"));
        assertNodesEquals("2\nA\nE",
                graph.nodes("?", "e1", null));
        assertNodesEquals("2\nB\nF",
                graph.nodes(null, "e1", "?"));
        assertNodesEquals("4\nA\nB\nE\nF",
                graph.nodes("?", "e1", "?"));

        StringGraphException e = assertThrows(StringGraphException.class,
                () -> graph.nodes("?", "?", "?"));
        assertEquals("Unsupported query: (\"?\", \"?\", \"?\")", e.getMessage());

        e = assertThrows(StringGraphException.class,
                () -> graph.nodes(null, null, null));
        assertEquals("Either `from` or `to` (or both) must be queried ('?')", e.getMessage());
    }

    /**
     * Returns a {@link StringGraph} with 6 Nodes (A - F) and
     * 5 edges with 3 different labels (e1 - e3).
     * <p>
     * Visually the graph looks like this:
     * <pre>
     *
     *
     *        A ——— e1 ——→ B
     *          ⟍        ↗︎
     *           e2    ⟋
     *              ⟍⟋
     *             ⟋  ⟍
     *           e3     ⟍
     *         ⟋         ↘︎
     *        C ——— e2 ——→ D
     *
     *        E ——— e1 ——→ F
     *
     * </pre>
     */
    private static StringGraph getSampleABCDEF() {
        StringGraphBuilder builder = StringGraphBuilderImpl.createStringGraphBuilder();
        builder.addEdge("A", "B", "e1");
        builder.addEdge("A", "D", "e2");
        builder.addEdge("C", "B", "e3");
        builder.addEdge("C", "D", "e2");
        builder.addEdge("E", "F", "e1");
        return builder.build();
    }

    @Test
    void edgesWithPattern() {
        StringGraph graph = getSampleABCDEF();

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("5\n" +
                        "\"A\" -> \"B\" : \"e1\"\n" +
                        "\"A\" -> \"D\" : \"e2\"\n" +
                        "\"C\" -> \"D\" : \"e2\"\n" +
                        "\"C\" -> \"B\" : \"e3\"\n" +
                        "\"E\" -> \"F\" : \"e1\"",
                graph.edges(null, null, null));

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("2\n" +
                        "\"A\" -> \"B\" : \"e1\"\n" +
                        "\"A\" -> \"D\" : \"e2\"",
                graph.edges("A", null, null));

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("2\n" +
                        "\"A\" -> \"B\" : \"e1\"\n" +
                        "\"E\" -> \"F\" : \"e1\"",
                graph.edges(null, "e1", null));

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("2\n" +
                        "\"A\" -> \"D\" : \"e2\"\n" +
                        "\"C\" -> \"D\" : \"e2\"",
                graph.edges(null, null, "D"));

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("1\n" +
                        "\"C\" -> \"B\" : \"e3\"",
                graph.edges("C", "e3", null));

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("1\n" +
                        "\"A\" -> \"D\" : \"e2\"",
                graph.edges("A", "e2", "D"));

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("0\n",
                graph.edges("X", "e2", "D"));
    }


    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void edges(StringGraph sample1) {
        assertEqualsToAllEdgesOfSample1(sample1.edges());
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allFromNodes(StringGraph sample1) {
        assertNodesAsLinesEquals("5\n" +
                        "c\n" +
                        "d\n" +
                        "f\n" +
                        "i\n" +
                        "o",
                sample1.fromNodes());
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allToNodes(StringGraph sample1) {
        assertNodesAsLinesEquals("7\n" +
                        "c\n" +
                        "e\n" +
                        "g\n" +
                        "i\n" +
                        "m1\n" +
                        "m2\n" +
                        "m3",
                sample1.toNodes());
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allEdgeLabels(StringGraph sample1) {
        assertStringsAsLinesEquals("4\n" +
                        "\n" +
                        "cycle\n" +
                        "field\n" +
                        "h",
                sample1.edgeLabels());
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allNodesFromNode(StringGraph sample1) {
        // node without edges
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNode("a"));
        // single node
        assertNodesAsLinesEquals("1\n" +
                        "g",
                sample1.nodesFromNode("f"));
        // multiple nodes
        assertNodesAsLinesEquals("3\n" +
                        "m1\n" +
                        "m2\n" +
                        "m3",
                sample1.nodesFromNode("o"));
        // self cyclic
        assertNodesAsLinesEquals("1\n" +
                        "c",
                sample1.nodesFromNode("c"));
        // toNode that is no fromNode
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNode("g"));
        // missing node
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNode("x"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allEdgeLabelsFromNode(StringGraph sample1) {
        // node without edges
        assertStringsAsLinesEquals("0\n", sample1.edgeLabelsFromNode("a"));
        // single node, empty label
        assertStringsAsLinesEquals("1\n" +
                        "",
                sample1.edgeLabelsFromNode("d"));
        // single node, non-empty label
        assertStringsAsLinesEquals("1\n" +
                        "h",
                sample1.edgeLabelsFromNode("f"));
        // multiple nodes
        assertStringsAsLinesEquals("2\n" +
                        "\n" +
                        "field",
                sample1.edgeLabelsFromNode("o"));
        // self cyclic
        assertStringsAsLinesEquals("1\n" +
                        "cycle",
                sample1.edgeLabelsFromNode("c"));
        // toNode that is no fromNode
        assertStringsAsLinesEquals("0\n", sample1.edgeLabelsFromNode("g"));
        // missing node
        assertStringsAsLinesEquals("0\n", sample1.edgeLabelsFromNode("x"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allNodesFromNodeViaEdgeLabeled(StringGraph sample1) {
        // node without edges
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNodeViaEdgeLabeled("a", ""));
        // single node, empty label
        assertNodesAsLinesEquals("1\n" +
                        "e",
                sample1.nodesFromNodeViaEdgeLabeled("d", ""));
        // single node, empty label, query wrong label
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNodeViaEdgeLabeled("d", "x"));
        // single node, non-empty label
        assertNodesAsLinesEquals("1\n" +
                        "g",
                sample1.nodesFromNodeViaEdgeLabeled("f", "h"));
        // single node, non-empty label, query wrong label
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNodeViaEdgeLabeled("f", "x"));
        // multiple nodes
        assertNodesAsLinesEquals("1\n" +
                        "m3",
                sample1.nodesFromNodeViaEdgeLabeled("o", ""));
        assertNodesAsLinesEquals("2\n" +
                        "m1\n" +
                        "m2",
                sample1.nodesFromNodeViaEdgeLabeled("o", "field"));
        // multiple nodes, query wrong label
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNodeViaEdgeLabeled("o", "x"));
        // self cyclic
        assertNodesAsLinesEquals("1\n" +
                        "c",
                sample1.nodesFromNodeViaEdgeLabeled("c", "cycle"));
        // self cyclic, query wrong label
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNodeViaEdgeLabeled("c", "x"));
        // toNode that is no fromNode
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNodeViaEdgeLabeled("g", "h"));
        // missing node
        assertNodesAsLinesEquals("0\n", sample1.nodesFromNodeViaEdgeLabeled("x", ""));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allNodesToNode(StringGraph sample1) {
        // node without edges
        assertNodesAsLinesEquals("0\n", sample1.nodesToNode("a"));
        // single node
        assertNodesAsLinesEquals("1\n" +
                        "d",
                sample1.nodesToNode("e"));
        // self cyclic
        assertNodesAsLinesEquals("1\n" +
                        "c",
                sample1.nodesToNode("c"));
        // fromNode that is no toNode
        assertNodesAsLinesEquals("0\n", sample1.nodesToNode("d"));
        // missing node
        assertNodesAsLinesEquals("0\n", sample1.nodesToNode("x"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allEdgeLabelsToNode(StringGraph sample1) {
        // node without edges
        assertStringsAsLinesEquals("0\n", sample1.edgeLabelsToNode("a"));
        // single node, empty label
        assertStringsAsLinesEquals("1\n" +
                        "",
                sample1.edgeLabelsToNode("e"));
        // single node, non-empty label
        assertStringsAsLinesEquals("1\n" +
                        "h",
                sample1.edgeLabelsToNode("g"));
        // self cyclic
        assertStringsAsLinesEquals("1\n" +
                        "cycle",
                sample1.edgeLabelsToNode("c"));
        // fromNode that is no toNode
        assertStringsAsLinesEquals("0\n", sample1.edgeLabelsToNode("d"));
        // missing node
        assertStringsAsLinesEquals("0\n", sample1.edgeLabelsToNode("x"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allNodesToNodeViaEdgeLabeled(StringGraph sample1) {
        // node without edges
        assertNodesAsLinesEquals("0\n", sample1.nodesToNodeViaEdgeLabeled("a", ""));
        // single node, empty label
        assertNodesAsLinesEquals("1\n" +
                        "d",
                sample1.nodesToNodeViaEdgeLabeled("e", ""));
        // single node, empty label, query wrong label
        assertNodesAsLinesEquals("0\n", sample1.nodesToNodeViaEdgeLabeled("e", "x"));
        // single node, non-empty label
        assertNodesAsLinesEquals("1\n" +
                        "f",
                sample1.nodesToNodeViaEdgeLabeled("g", "h"));
        // single node, non-empty label, query wrong label
        assertNodesAsLinesEquals("0\n", sample1.nodesToNodeViaEdgeLabeled("g", "x"));
        // self cyclic
        assertNodesAsLinesEquals("1\n" +
                        "c",
                sample1.nodesToNodeViaEdgeLabeled("c", "cycle"));
        // self cyclic, query wrong label
        assertNodesAsLinesEquals("0\n", sample1.nodesToNodeViaEdgeLabeled("c", "x"));
        // fromNode that is no toNode
        assertNodesAsLinesEquals("0\n", sample1.nodesToNodeViaEdgeLabeled("f", "h"));
        // missing node
        assertNodesAsLinesEquals("0\n", sample1.nodesToNodeViaEdgeLabeled("x", ""));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allEdgesWith(StringGraph sample1) {
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("3\n" +
                        "\"o\" -> \"m3\"\n" +
                        "\"o\" -> \"m1\" : \"field\"\n" +
                        "\"o\" -> \"m2\" : \"field\"",
                sample1.edgesWith(e -> e.getToNode().id().startsWith("m")));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("1\n" +
                        "\"f\" -> \"g\" : \"h\"",
                sample1.edgesWith(e -> e.getLabel().equals("h")));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("0\n",
                sample1.edgesWith(e -> false));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allEdgesLabeled(StringGraph sample1) {
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("2\n" +
                        "\"d\" -> \"e\"\n" +
                        "\"o\" -> \"m3\"",
                sample1.edgesLabeled(""));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("2\n" +
                        "\"c\" -> \"c\" : \"cycle\"\n" +
                        "\"i\" -> \"i\" : \"cycle\"",
                sample1.edgesLabeled("cycle"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("0\n",
                sample1.edgesLabeled("x"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allEdgesFromNode(StringGraph sample1) {
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("0\n",
                sample1.edgesFromNode("a"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("1\n" +
                        "\"c\" -> \"c\" : \"cycle\"",
                sample1.edgesFromNode("c"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("1\n" +
                        "\"d\" -> \"e\"",
                sample1.edgesFromNode("d"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("3\n" +
                        "\"o\" -> \"m3\"\n" +
                        "\"o\" -> \"m1\" : \"field\"\n" +
                        "\"o\" -> \"m2\" : \"field\"",
                sample1.edgesFromNode("o"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("0\n",
                sample1.edgesFromNode("x"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void allEdgesToNode(StringGraph sample1) {
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("0\n",
                sample1.edgesToNode("a"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("1\n" +
                        "\"c\" -> \"c\" : \"cycle\"",
                sample1.edgesToNode("c"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("1\n" +
                        "\"d\" -> \"e\"",
                sample1.edgesToNode("e"));
        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("0\n",
                sample1.edgesFromNode("x"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphBuilderProvider")
    void equalsAndHashcode(StringGraphBuilder builder) {
        StringGraph sample1 = constructSample1(builder).build();
        StringGraph otherSample1 = constructSample1(builder).build();
        int h1 = sample1.hashCode();
        int h2 = otherSample1.hashCode();

        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(sample1, "not a graph");
        assertNotEquals(sample1, null);
        assertEquals(sample1, sample1);
        assertEquals(sample1, otherSample1);
        assertEquals(h1, h2);
    }

    @ParameterizedTest
    @MethodSource("stringGraphSample1Provider")
    void toStringTest(StringGraph sample1) {
        String s = sample1.toString();

        assertTrue(s.startsWith("StringGraph"));
    }

    @ParameterizedTest
    @MethodSource("stringGraphBuilderProvider")
    void duplicateEdges(StringGraphBuilder builder) {
        builder.addEdge("a", "b", "c");
        builder.addEdge("d", "e", "f");
        // adding an edge that already exists will not change the graph
        builder.addEdge("a", "b", "c");
        StringGraph graph = builder.build();

        EdgeDefaultTest.assertEdgesEqualsIgnoreOrder("2\n" +
                "\"a\" -> \"b\" : \"c\"\n" +
                "\"d\" -> \"e\" : \"f\"", graph.edges());
    }

    @ParameterizedTest
    @MethodSource("stringGraphBuilderProvider")
    void props(StringGraphBuilder builder) {
        builder.addEdge("a", "b", "c");

        builder.setNodeProperty("a", "prop1", "foo");
        builder.setNodeProperty("a", "prop2", "bar");
        // Node must exists to get a prop value
        StringGraphException e = assertThrows(StringGraphException.class,
                () -> builder.setNodeProperty("d", "prop1", "2"));
        assertEquals("Error when setting node property. Node does not exist: d", e.getMessage());

        StringGraph graph = builder.build();

        // StringGraph

        Node nodeA = graph.nodes("?", "c", "b")
                .stream().findFirst().orElseThrow(IllegalStateException::new);
        Node nodeB = graph.nodes("a", "c", "?")
                .stream().findFirst().orElseThrow(IllegalStateException::new);

        // ... hasNodeProperty
        assertTrue(graph.hasNodeProperty("a", "prop1"));
        assertFalse(graph.hasNodeProperty("a", "missingProp"));
        assertFalse(graph.hasNodeProperty("b", "missingProp"));

        // ... hasProperty
        assertTrue(graph.hasProperty(nodeA, "prop1"));
        assertFalse(graph.hasProperty(nodeA, "missingProp"));
        assertFalse(graph.hasProperty(nodeB, "missingProp"));

        // ... getNodePropertyValue
        assertEquals("foo", graph.getNodePropertyValue("a", "prop1"));
        assertEquals("bar", graph.getNodePropertyValue("a", "prop2"));
        e = assertThrows(StringGraphException.class,
                () -> graph.getNodePropertyValue("b", "prop1"));
        assertEquals("No such property: prop1", e.getMessage());

        // ... getPropertyValue
        assertEquals("foo", graph.getPropertyValue(nodeA, "prop1"));
        assertEquals("bar", graph.getPropertyValue(nodeA, "prop2"));
        e = assertThrows(StringGraphException.class,
                () -> graph.getPropertyValue(nodeB, "prop1"));
        assertEquals("No such property: prop1", e.getMessage());

        // ... getNodePropertyValue
        assertEquals("foo", graph.getNodePropertyValueOrElse("a", "prop1", "baz"));
        assertEquals("baz", graph.getNodePropertyValueOrElse("b", "prop1", "baz"));

        // ... getPropertyValue
        assertEquals("foo", graph.getPropertyValueOrElse(nodeA, "prop1", "baz"));
        assertEquals("baz", graph.getPropertyValueOrElse(nodeB, "prop1", "baz"));

        // ... getNodeProperty
        Property prop1OfA = graph.getNodeProperty("a", "prop1");
        Property prop2OfA = graph.getNodeProperty("a", "prop2");
        e = assertThrows(StringGraphException.class,
                () -> graph.getNodeProperty("b", "prop1"));
        assertEquals("No such property: prop1", e.getMessage());

        // ... getProperty
        Property prop1OfA2 = graph.getProperty(nodeA, "prop1");
        Property prop2OfA2 = graph.getProperty(nodeA, "prop2");
        e = assertThrows(StringGraphException.class,
                () -> graph.getProperty(nodeB, "prop1"));
        assertEquals("No such property: prop1", e.getMessage());

        // Property

        // ... toString
        assertEquals("PropertyImpl{name='prop1', value='foo'}", prop1OfA.toString());

        // ... equals/hashCode
        assertNotEquals(prop1OfA, prop2OfA);
        assertNotEquals(prop1OfA2, prop2OfA2);
        assertNotEquals(prop1OfA.hashCode(), prop2OfA.hashCode());
        assertNotEquals(prop1OfA2.hashCode(), prop2OfA2.hashCode());

        // Properties

        // ... getNodeProperties + getValueOfProperty
        Properties ps = graph.getNodeProperties("a");
        assertEquals("foo", ps.getValueOfProperty("prop1"));
        
        // ... getProperties + getValueOfProperty
        ps = graph.getProperties(nodeA);
        assertEquals("foo", ps.getValueOfProperty("prop1"));

        // ... iterator
        StringBuilder sb = new StringBuilder();
        for (Property p : ps) {
            sb.append(p.getName());
            sb.append("=");
            sb.append(p.getValue());
            sb.append(";");
        }
        assertEquals("prop1=foo;prop2=bar;", sb.toString());
        // ... iterator (next)
        assertThrows(NoSuchElementException.class, () ->
                graph.getNodeProperties("noNode").iterator().next());
    }


}
