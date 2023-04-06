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

package org.abego.stringgraph.internal;

import org.abego.stringgraph.core.Edge;
import org.abego.stringgraph.core.EdgeLabels;
import org.abego.stringgraph.core.Edges;
import org.abego.stringgraph.core.NoSuchPropertyException;
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;
import org.abego.stringgraph.core.Properties;
import org.abego.stringgraph.core.Property;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphException;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.abego.stringgraph.internal.ArrayUtil.intArray;
import static org.abego.stringgraph.internal.EdgeUtil.calcEdgeText;
import static org.abego.stringgraph.internal.EmptyEdges.EMPTY_EDGES;
import static org.abego.stringgraph.internal.EmptyNodes.EMPTY_NODES;
import static org.abego.stringgraph.internal.StringUtil.quoted2;
import static org.abego.stringgraph.internal.StringUtil.quotedIfNeeded;

public class StringGraphImpl implements StringGraph {

    private enum PatternKind {
        QUERY,
        BOUND,
        NULL;

        static PatternKind of(@Nullable String pattern) {
            return pattern == null ? PatternKind.NULL :
                    pattern.startsWith("?") ? PatternKind.QUERY :
                            PatternKind.BOUND;
        }
    }


    private class MyNode implements Node {
        private final int id;

        private MyNode(int id) {
            this.id = id;
        }

        @Override
        public String id() {
            return state.getString(id);
        }

        @Override
        public String getText() {
            return quotedIfNeeded(id());
        }

        @Override
        public int compareTo(Node o) {
            // shortcut for the "equals" case.
            if (o instanceof MyNode) {
                if (((MyNode) o).id == id) {
                    return 0;
                }
            }

            return id().compareTo(o.id());
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyNode myNode = (MyNode) o;
            return id == myNode.id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return "MyNode{" + "id=" + quoted2(id()) + "}";
        }

    }

    private class MyNodes implements Nodes {
        private final int[] nodesIDs;
        private boolean isSorted = false;

        private MyNodes(int[] nodesIDs) {
            this.nodesIDs = nodesIDs;
        }

        @Override
        public int getSize() {
            return nodesIDs.length;
        }

        @Override
        public Stream<Node> stream() {
            return Arrays.stream(nodesIDs).mapToObj(MyNode::new);
        }

        @Override
        public Nodes intersected(Nodes otherNodes) {
            if (otherNodes == this) {
                return this;
            }

            // sort both id arrays
            int[] a = sortedIds();
            int[] b = asMyNodes(otherNodes).sortedIds();
            // compare the items of both id arrays and add all ids that are
            // in both array to the buffer array
            int nA = a.length;
            int nB = b.length;
            int maxResultSize = Math.min(nA, nB);
            int[] buffer = new int[maxResultSize];
            int iBuffer = 0;
            int iA = 0;
            int iB = 0;
            while (iA < nA && iB < nB) {
                int vA = a[iA];
                int vB = b[iB];
                if (vA == vB) {
                    buffer[iBuffer++] = vA;
                    iA++;
                    iB++;
                } else if (vA > vB) {
                    //noinspection StatementWithEmptyBody
                    while (++iB < nB && vA > b[iB]) {
                        // empty by intend
                    }
                } else {
                    // vB > vA
                    //noinspection StatementWithEmptyBody
                    while (++iA < nA && vB > a[iA]) {
                        // empty by intend
                    }
                }
            }
            return new MyNodes(Arrays.copyOf(buffer, iBuffer));
        }

        private int[] sortedIds() {
            if (!isSorted) {
                Arrays.sort(nodesIDs);
                isSorted = true;
            }
            return nodesIDs;
        }

        @Override
        public Iterator<Node> iterator() {
            return stream().iterator();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyNodes nodes = (MyNodes) o;
            return Arrays.equals(nodesIDs, nodes.nodesIDs);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(nodesIDs);
        }
    }

    private class MyEdge implements Edge {
        private final int edgesOffset;

        private MyEdge(int edgesOffset) {
            this.edgesOffset = edgesOffset;
        }

        @Override
        public Node getFromNode() {
            return new MyNode(state.getFromId(edgesOffset));
        }

        @Override
        public String getLabel() {
            return state.getString(getLabelId());
        }

        private int getLabelId() {
            return state.getLabelId(edgesOffset);
        }

        @Override
        public Node getToNode() {
            return new MyNode(state.getToId(edgesOffset));
        }

        @Override
        public String getText() {
            return calcEdgeText(this);
        }

        @Override
        public int compareTo(Edge o) {
            return EdgeUtil.getComparator().compare(this, o);
        }

        @Override
        public String toString() {
            //noinspection HardCodedStringLiteral,StringConcatenation,MagicCharacter
            return "MyEdge{" +
                    "fromNode=" + quoted2(getFromNode().id()) +
                    ", label=" + quoted2(getLabel()) +
                    ", toNode=" + quoted2(getToNode().id()) +
                    "}";
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyEdge myEdge = (MyEdge) o;
            return edgesOffset == myEdge.edgesOffset;
        }

        @Override
        public int hashCode() {
            return edgesOffset;
        }
    }

    private class MyEdges implements Edges {

        //TODO: consistently use "edgesIds", not "edgesOffsets"
        private final int[] edgesOffsets;

        private MyEdges(int[] edgesOffsets) {
            this.edgesOffsets = edgesOffsets;
        }

        @Override
        public int getSize() {
            return edgesOffsets.length;
        }

        @Override
        public boolean contains(Edge edge) {
            int id = asMyEdge(edge).edgesOffset;
            //TODO: more efficient implementation?
            for (int edgesOffset : edgesOffsets) {
                if (edgesOffset == id) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean contains(String fromNode, String label, String toNode) {
            int fromId = state.getStringIdOrZero(fromNode);
            if (fromId == 0) {
                return false;
            }
            int labelId = state.getStringIdOrZero(label);
            if (labelId == 0) {
                return false;
            }
            int toId = state.getStringIdOrZero(toNode);
            if (toId == 0) {
                return false;
            }
            //TODO: more efficient implementation?
            for (int edgesOffset : edgesOffsets) {
                if (fromId == state.getFromId(edgesOffset) &&
                        toId == state.getToId(edgesOffset) &&
                        labelId == state.getLabelId(edgesOffset)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Stream<Edge> stream() {
            return Arrays.stream(edgesOffsets).mapToObj(MyEdge::new);
        }

        @Override
        public Edges filtered(Predicate<Edge> edgePredicate) {
            return createMyEdges(stream().filter(edgePredicate)
                    .collect(Collectors.toSet()));
        }

        private MyEdges createMyEdges(Set<Edge> edges) {
            int n = edges.size();
            int[] resultIds = new int[n];
            int i = 0;
            for (Edge edge : edges) {
                resultIds[i++] = asMyEdge(edge).edgesOffset;
            }
            return new MyEdges(resultIds);
        }

        @Override
        public Edges intersected(Edges otherEdges) {
            //TODO copy/paste code from EdgesImpl

            // find the Edges object with fewer items, to iterate over that object
            Edges fewerEdges;
            Edges moreEdges;
            if (getSize() < otherEdges.getSize()) {
                fewerEdges = this;
                moreEdges = otherEdges;
            } else {
                fewerEdges = otherEdges;
                moreEdges = this;
            }

            // All items in the fewerEdges also contains in moreEdges are included
            HashSet<Edge> result = new HashSet<>();
            for (Edge e : fewerEdges) {
                if (moreEdges.contains(e)) {
                    result.add(e);
                }
            }

            return createMyEdges(result);
        }

        @Override
        public Iterable<Edge> sorted(Comparator<? super Edge> comparator) {
            return stream().sorted(comparator).collect(Collectors.toList());
        }

        @Override
        public Iterable<Edge> sorted() {
            return sorted(EdgeUtil.getComparator());
        }

        @Override
        public Iterator<Edge> iterator() {
            return stream().iterator();
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyEdges edges = (MyEdges) o;
            return Arrays.equals(edgesOffsets, edges.edgesOffsets);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(edgesOffsets);
        }
    }

    private class MyProperty implements Property {

        private final int nameId;
        private final int valueId;

        public MyProperty(int nameId, int valueId) {
            this.nameId = nameId;
            this.valueId = valueId;
        }

        @Override
        public String getName() {
            return state.getString(nameId);
        }

        @Override
        public String getValue() {
            return state.getString(valueId);
        }

        @Override
        public String toString() {
            return "MyProperty{" +
                    "name=" + quoted2(getName()) +
                    ", value=" + quoted2(getValue()) +
                    '}';
        }

        @Override
        public int compareTo(Property o) {
            return PROPERTY_COMPARATOR.compare(this, o);
        }
    }

    private class MyProperties implements Properties {
        private final int[] keyValueIds;

        public MyProperties(int[] keyValueIds) {
            this.keyValueIds = keyValueIds;
        }

        @Override
        public int getSize() {
            return keyValueIds.length / 2;
        }

        @Override
        public Stream<Property> stream() {
            return IntStream.range(0, getSize())
                    .mapToObj(i -> new MyProperty(keyValueIds[2 * i], keyValueIds[2 * i + 1]));
        }

        @Override
        public boolean hasProperty(String name) {
            int nameId = state.getStringIdOrZero(name);
            return nameId != 0 && IntStream.range(0, getSize())
                    .anyMatch(i -> keyValueIds[2 * i] == nameId);
        }

        @Override
        public @Nullable Property getPropertyOrNull(String name) {
            int nameId = state.getStringId(name);
            return IntStream.range(0, getSize())
                    .filter(i -> keyValueIds[2 * i] == nameId)
                    .mapToObj(i -> new MyProperty(keyValueIds[2 * i], keyValueIds[2 * i + 1]))
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Property getProperty(String name) {
            Property property = getPropertyOrNull(name);
            if (property == null) {
                throw new NoSuchPropertyException(name);
            }
            return property;
        }

        @Override
        public String getValueOfProperty(String name) {
            return getProperty(name).getValue();
        }

        @Override
        public String getValueOfPropertyOrElse(String name, String defaultValue) {
            Property p = getPropertyOrNull(name);
            return p != null ? p.getValue() : defaultValue;
        }

        @Override
        public Iterator<Property> iterator() {
            return stream().iterator();
        }
    }

    private class EdgesIndex {
        private final Map<Integer, List<Integer>> map = new HashMap<>();
        private final Map<Integer, Edges> edgesMap = new HashMap<>();

        public void add(int key, int edgeId) {
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(edgeId);
        }

        public Edges edges(int key) {
            return key != 0
                    ? edgesMap.computeIfAbsent(key, k -> new MyEdges(intArray(map.get(k))))
                    : EMPTY_EDGES;
        }

        public Edges edges(Node node) {
            return edges(asMyNode(node).id);
        }

        public Edges edges(String string) {
            return edges(state.getStringIdOrZero(string));
        }

        public int[] keys() {
            return intArray(map.keySet());
        }

        public Set<String> keyStrings() {
            return map.keySet().stream()
                    .map(state::getString)
                    .collect(Collectors.toSet());
        }
    }


    private static final Comparator<Property> PROPERTY_COMPARATOR =
            Comparator.comparing(Property::getName)
                    .thenComparing(Property::getValue);
    private final Properties EMPTY_PROPERTIES = new MyProperties(new int[0]);

    private final StringGraphState state;
    private final Set<Integer> nodeIds;
    /**
     * Links every fromNode to the Edges it belongs to.
     */
    private final EdgesIndex edgesIndexForFromNode = new EdgesIndex();
    /**
     * Links every toNode to the Edges it belongs to.
     */
    private final EdgesIndex edgesIndexForToNode = new EdgesIndex();
    /**
     * Links every label to the Edges it belongs to.
     */
    private final EdgesIndex edgesIndexForLabel = new EdgesIndex();
    @Nullable
    private Nodes fromNodes;
    @Nullable
    private Nodes toNodes;

    private StringGraphImpl(StringGraphState state) {
        this.state = state;
        this.nodeIds = new HashSet<>();
        for (int i : state.getNodesIds()) {
            nodeIds.add(i);
        }

        int edgesCount = state.getEdgesCount();
        for (int i = 0; i < edgesCount; i++) {
            int edgeId = i * 3;//TODO: internal know how. Avoid this.
            edgesIndexForFromNode.add(state.getFromId(edgeId), edgeId);
            edgesIndexForToNode.add(state.getToId(edgeId), edgeId);
            edgesIndexForLabel.add(state.getLabelId(edgeId), edgeId);
        }
    }

    public static StringGraph createStringGraph(StringGraphState data) {
        return new StringGraphImpl(data);
    }

    @Override
    public Nodes fromNodes() {
        if (fromNodes == null) {
            fromNodes = new MyNodes(edgesIndexForFromNode.keys());
        }
        return fromNodes;
    }

    @Override
    public Nodes nodes() {
        return new MyNodes(state.getNodesIds());
    }

    @Override
    @SuppressWarnings({"PointlessArithmeticExpression", "DataFlowIssue"})
    public Nodes nodes(@Nullable String fromPattern,
                       @Nullable String labelPattern,
                       @Nullable String toPattern) {

        PatternKind fromKind = PatternKind.of(fromPattern);
        PatternKind labelKind = PatternKind.of(labelPattern);
        PatternKind toKind = PatternKind.of(toPattern);

        if (fromKind != PatternKind.QUERY && toKind != PatternKind.QUERY) {
            throw new StringGraphException(
                    "Either `from` or `to` (or both) must be queried ('?')");
        }

        int combinationIndex = fromKind.ordinal()
                + 3 * labelKind.ordinal()
                + 9 * toKind.ordinal();
        switch (combinationIndex) {
            case 0 + 3 * 2 + 9 * 2: // "?", null, null
                return fromNodes();
            case 2 + 3 * 2 + 9 * 0: // null, null, "?"
                return toNodes();
            case 0 + 3 * 2 + 9 * 0: // "?", null, "?"
                return nodes();
            case 0 + 3 * 2 + 9 * 1: // "?", null, "ABC"
                return nodesToNode(toPattern);
            case 1 + 3 * 2 + 9 * 0: // "ABC", null, "?"
                return nodesFromNode(fromPattern);
            case 0 + 3 * 1 + 9 * 1: // "?", "lab", "ABC"
                return nodesViaEdgeLabeledToNode(labelPattern, toPattern);
            case 1 + 3 * 1 + 9 * 0: // "ABC", "lab", "?"
                return nodesFromNodeViaEdgeLabeled(fromPattern, labelPattern);
            case 0 + 3 * 1 + 9 * 2: // "?", "lab", null
                return asNodes(
                        edgesIndexForLabel.edges(labelPattern).stream()
                                .map(Edge::getFromNode)
                                .collect(Collectors.toSet()));
            case 2 + 3 * 1 + 9 * 0: // null, "lab", "?"
                return asNodes(
                        edgesIndexForLabel.edges(labelPattern).stream()
                                .map(Edge::getToNode)
                                .collect(Collectors.toSet()));
            case 0 + 3 * 1 + 9 * 0: // "?", "lab", "?"
                Set<Node> nodes = new HashSet<>();
                edgesIndexForLabel.edges(labelPattern).forEach(edge -> {
                    nodes.add(edge.getFromNode());
                    nodes.add(edge.getToNode());
                });
                return asNodes(nodes);
            default:
                throw new StringGraphException(String.format(
                        "Unsupported query: (%s, %s, %s)",
                        StringUtil.quoted2(fromPattern),
                        StringUtil.quoted2(labelPattern),
                        StringUtil.quoted2(toPattern)));
        }
    }

    @Override
    public Edges edges() {
        //TODO: no nice code
        int[] result = new int[state.getEdgesCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i * 3;
        }
        return new MyEdges(result);
    }

    @Override
    public Edges edges(@Nullable String from, @Nullable String label, @Nullable String to) {
        List<Edges> edgesByPart = new ArrayList<>();
        if (from != null) {
            if (!hasNode(from)) {
                return EMPTY_EDGES;
            }
            edgesByPart.add(edgesFromNode(from));
        }
        if (label != null) {
            edgesByPart.add(edgesLabeled(label));
        }
        if (to != null) {
            if (!hasNode(to)) {
                return EMPTY_EDGES;
            }
            edgesByPart.add(edgesToNode(to));
        }

        if (edgesByPart.isEmpty()) {
            // no restriction -> all edges
            return edges();
        }

        Edges result = edgesByPart.get(0);
        for (int i = 1; i < edgesByPart.size(); i++) {
            result = result.intersected(edgesByPart.get(i));
        }

        return result;
    }

    @Override
    public Properties getNodeProperties(String node) {
        return getNodeProperties().apply(node);
    }

    @Override
    public boolean hasNodeProperty(String node, String propertyName) {
        return getNodeProperties(node).hasProperty(propertyName);
    }

    @Override
    public Property getNodeProperty(String node, String propertyName) {
        return getNodeProperties(node).getProperty(propertyName);
    }

    @Override
    public String getNodePropertyValue(String node, String propertyName) {
        return getNodeProperty(node, propertyName).getValue();
    }

    @Override
    public String getNodePropertyValueOrElse(String node, String propertyName, String defaultValue) {
        return getNodeProperties(node).getValueOfPropertyOrElse(propertyName, defaultValue);
    }

    @Override
    public Nodes toNodes() {
        if (toNodes == null) {
            toNodes = new MyNodes(edgesIndexForToNode.keys());
        }
        return toNodes;
    }

    @Override
    public EdgeLabels edgeLabels() {
        return EdgeLabelsImpl.createEdgeLabels(edgesIndexForLabel.keyStrings());
    }

    @Override
    public Nodes nodesFromNode(String fromNode) {
        return selectNodes(fromNode,
                edgesIndexForFromNode::edges,
                e -> true,
                Edge::getToNode);
    }

    @Override
    public EdgeLabels edgeLabelsFromNode(String fromNode) {
        return valueWithNodeOrElse(
                fromNode,
                n -> EdgeLabelsImpl.createEdgeLabels(
                        edgesIndexForFromNode.edges(n).stream()
                                .map(Edge::getLabel)
                                .collect(Collectors.toSet())),
                EdgeLabelsImpl.EMPTY_EDGE_LABELS);

    }

    @Override
    public Nodes nodesFromNodeViaEdgeLabeled(
            String fromNode, String edgeLabel) {
        return selectNodes(fromNode,
                edgesIndexForFromNode::edges,
                e -> e.getLabel().equals(edgeLabel),
                Edge::getToNode);
    }

    @Override
    public Nodes nodesToNode(String toNode) {
        return selectNodes(toNode,
                edgesIndexForToNode::edges,
                e -> true,
                Edge::getFromNode);
    }

    @Override
    public EdgeLabels edgeLabelsToNode(String toNode) {
        return valueWithNodeOrElse(
                toNode,
                n -> EdgeLabelsImpl.createEdgeLabels(
                        edgesIndexForToNode.edges(n).stream()
                                .map(Edge::getLabel)
                                .collect(Collectors.toSet())),
                EdgeLabelsImpl.EMPTY_EDGE_LABELS);

    }

    @Override
    public Nodes nodesViaEdgeLabeledToNode(String edgeLabel, String toNode) {
        try {
            Edges edges = edgesIndexForToNode.edges(toNode);
            return fromNodeOfEdgesWithLabel(edges, edgeLabel);
        } catch (NoSuchElementException e) {
            return EmptyNodes.EMPTY_NODES;
        }
    }

    @Override
    public Edges edgesWith(Predicate<Edge> edgePredicate) {
        return edges().filtered(edgePredicate);
    }

    @Override
    public Edges edgesLabeled(String edgeLabel) {
        return edgesIndexForLabel.edges(edgeLabel);
    }

    @Override
    public Edges edgesFromNode(String fromNode) {
        return valueWithNodeOrElse(
                fromNode, edgesIndexForFromNode::edges, EMPTY_EDGES);
    }

    @Override
    public Edges edgesToNode(String toNode) {
        return valueWithNodeOrElse(
                toNode, edgesIndexForToNode::edges, EMPTY_EDGES);
    }

    @Override
    public boolean hasEdge(String fromNode, String edgeLabel, String toNode) {
        return edges().contains(fromNode, edgeLabel, toNode);
    }

    @Override
    public String toString() {
        return "StringGraphImpl{" + "state=" + state + '}';
    }

    public Function<String, Properties> getNodeProperties() {
        return nodeId -> {
            int id = state.getStringId(nodeId);
            int[] ps = state.getPropertyDataForNode(id);
            if (ps == null) {
                return EMPTY_PROPERTIES;
            }
            return new MyProperties(ps);
        };
    }

    public Node getNode(String id) {
        int stringId = state.getStringId(id);
        if (!nodeIds.contains(stringId)) {
            throw new NoSuchElementException();
        }
        return new MyNode(stringId);
    }

    public Nodes fromNodeOfEdgesWithLabel(Edges edges, String edgeLabel) {
        int edgesSize = edges.getSize();
        int labelId = state.getStringIdOrZero(edgeLabel);
        if (labelId == 0 || edgesSize == 0) {
            return EMPTY_NODES;
        }
        int[] buffer = new int[edgesSize];
        int i = 0;
        for (int id : asMyEdges(edges).edgesOffsets) {
            if (state.getLabelId(id) == labelId) {
                buffer[i++] = state.getFromId(id);
            }
        }
        return new MyNodes(Arrays.copyOf(buffer, i));
    }

    private Nodes asNodes(Set<Node> nodes) {
        if (nodes.isEmpty()) {
            return EMPTY_NODES;
        }

        int[] nodesIDs = nodes.stream().mapToInt(e -> {
            if (!(e instanceof MyNode)) {
                throw new IllegalArgumentException("MyNode expected, got " + e.getClass());
            }
            return ((MyNode) e).id;
        }).toArray();
        return new MyNodes(nodesIDs);
    }

    private boolean hasNode(String id) {
        int stringId = state.getStringIdOrZero(id);
        return stringId != 0 && nodeIds.contains(stringId);
    }

    /**
     * Uses the {@code edgeCondition} to filter all edges returned by the
     * edgesProvider applied on the Node identified by the {@code nodeId} and
     * returns all nodes from those filtered edges selected through the
     * {@code nodeSelect} function, or no nodes, when {@code nodeId} does not
     * identify a node.
     */
    private Nodes selectNodes(
            String nodeId,
            Function<Node, Edges> edgesProvider,
            Predicate<Edge> edgeCondition,
            Function<Edge, Node> nodeSelect) {

        return valueWithNodeOrElse(
                nodeId,
                n -> asNodes(
                        edgesProvider.apply(n).stream()
                                .filter(edgeCondition)
                                .map(nodeSelect)
                                .collect(Collectors.toSet())),
                EmptyNodes.EMPTY_NODES);
    }

    /**
     * Returns the value of the function applied on the node identified by the
     * {@code nodeId} or the {@code elseValue} when nodeId does not identify
     * a node (i.e. {@code hasNode(nodeId)} returns {@code false}).
     */
    private <T> T valueWithNodeOrElse(
            String nodeId, Function<Node, T> function, T elseValue) {
        return hasNode(nodeId) ? function.apply(getNode(nodeId)) : elseValue;
    }

    private static MyNode asMyNode(Node node) {
        if (!(node instanceof MyNode)) {
            throw new IllegalArgumentException("MyNode expected, got " + node.getClass());
        }
        return (MyNode) node;
    }

    private static MyEdge asMyEdge(Edge edge) {
        if (!(edge instanceof MyEdge)) {
            throw new IllegalArgumentException("MyEdge expected, got " + edge.getClass());
        }
        return (MyEdge) edge;
    }

    private static MyEdges asMyEdges(Edges edges) {
        if (!(edges instanceof MyEdges)) {
            throw new IllegalArgumentException("MyEdges expected, got " + edges.getClass());
        }
        return (MyEdges) edges;
    }

    private static MyNodes asMyNodes(Nodes nodes) {
        if (!(nodes instanceof MyNodes)) {
            throw new IllegalArgumentException("MyNodes expected, got " + nodes.getClass());
        }
        return (MyNodes) nodes;
    }


}
