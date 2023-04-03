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

import org.abego.stringgraph.core.Edge;
import org.abego.stringgraph.core.Edges;
import org.abego.stringgraph.core.NoSuchPropertyException;
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;
import org.abego.stringgraph.core.Properties;
import org.abego.stringgraph.core.Property;
import org.abego.stringgraph.core.StringGraphConstructing;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.abego.stringgraph.internal.EdgeUtil.calcEdgeText;
import static org.abego.stringgraph.internal.EmptyEdges.EMPTY_EDGES;
import static org.abego.stringgraph.internal.EmptyNodes.EMPTY_NODES;
import static org.abego.stringgraph.internal.StringUtil.quoted2;
import static org.abego.stringgraph.internal.StringUtil.quotedIfNeeded;

public class StringGraphData {
    private static final Comparator<Property> PROPERTY_COMPARATOR =
            Comparator.comparing(Property::getName)
                    .thenComparing(Property::getValue);

    private final Properties EMPTY_PROPERTIES = new MyProperties(new int[0]);

    private final StringGraphDataProvider provider;
    private final Set<Integer> nodeIds;

    /**
     * Links every fromNode to the Edges it belongs to.
     */
    final EdgesIndex edgesIndexForFromNode = new EdgesIndex();
    /**
     * Links every toNode to the Edges it belongs to.
     */
    final EdgesIndex edgesIndexForToNode = new EdgesIndex();
    /**
     * Links every label to the Edges it belongs to.
     */
    final EdgesIndex edgesIndexForLabel = new EdgesIndex();


    private class MyNode implements Node {
        private final int id;

        private MyNode(int id) {
            this.id = id;
        }

        @Override
        public String id() {
            return provider.getString(id);
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
            return new MyNode(provider.getFromId(edgesOffset));
        }

        @Override
        public String getLabel() {
            return provider.getString(getLabelId());
        }

        private int getLabelId() {
            return provider.getLabelId(edgesOffset);
        }

        @Override
        public Node getToNode() {
            return new MyNode(provider.getToId(edgesOffset));
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
            int fromId = provider.getStringIdOrZero(fromNode);
            if (fromId == 0) {
                return false;
            }
            int labelId = provider.getStringIdOrZero(label);
            if (labelId == 0) {
                return false;
            }
            int toId = provider.getStringIdOrZero(toNode);
            if (toId == 0) {
                return false;
            }
            //TODO: more efficient implementation?
            for (int edgesOffset : edgesOffsets) {
                if (fromId == provider.getFromId(edgesOffset) &&
                        toId == provider.getToId(edgesOffset) &&
                        labelId == provider.getLabelId(edgesOffset)) {
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
            return provider.getString(nameId);
        }

        @Override
        public String getValue() {
            return provider.getString(valueId);
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
            int nameId = provider.getStringIdOrZero(name);
            return nameId != 0 && IntStream.range(0, getSize())
                    .anyMatch(i -> keyValueIds[2 * i] == nameId);
        }

        @Override
        public @Nullable Property getPropertyOrNull(String name) {
            int nameId = provider.getStringId(name);
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

    class EdgesIndex {
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
            return edges(provider.getStringIdOrZero(string));
        }

        public int[] keys() {
            return intArray(map.keySet());
        }

        public Set<String> keyStrings() {
            return map.keySet().stream()
                    .map(provider::getString)
                    .collect(Collectors.toSet());
        }
    }

    private StringGraphData(StringGraphDataProvider provider) {
        this.provider = provider;
        this.nodeIds = new HashSet<>();
        for (int i : provider.getNodesIds()) {
            nodeIds.add(i);
        }

        int edgesCount = provider.getEdgesCount();
        for (int i = 0; i < edgesCount; i++) {
            int edgeId = i * 3;//TODO: internal know how. Avoid this.
            edgesIndexForFromNode.add(provider.getFromId(edgeId), edgeId);
            edgesIndexForToNode.add(provider.getToId(edgeId), edgeId);
            edgesIndexForLabel.add(provider.getLabelId(edgeId), edgeId);
        }
    }

    public static StringGraphData createStringGraphData(StringGraphDataProvider provider) {
        return new StringGraphData(provider);
    }
    
    public Nodes asNodes(Set<Node> nodes) {
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

    public Nodes getNodes() {
        return new MyNodes(provider.getNodesIds());
    }

    public Edges getEdges() {
        //TODO: no nice code
        int[] result = new int[provider.getEdgesCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i * 3;
        }
        return new MyEdges(result);
    }

    public Function<String, Properties> getNodeProperties() {
        return nodeId -> {
            int id = provider.getStringId(nodeId);
            int[] ps = provider.getPropertyDataForNode(id);
            if (ps == null) {
                return EMPTY_PROPERTIES;
            }
            return new MyProperties(ps);
        };
    }

    public void addNodes(StringGraphConstructing graphConstructing) {
        for (int nodesID : provider.getNodesIds()) {
            graphConstructing.addNode(provider.getString(nodesID));
        }
    }

    public void addEdges(StringGraphConstructing graphConstructing) {
        int n = provider.getEdgesCount();
        for (int i = 0; i < n; i += 3) {
            graphConstructing.addEdge(
                    provider.getString(provider.getFromId(i)),
                    provider.getString(provider.getLabelId(i)),
                    provider.getString(provider.getToId(i))
            );
        }
    }

    public void addProps(StringGraphConstructing graphConstructing) {
        for (Map.Entry<Integer, int[]> e : provider.getAllProperties()) {
            int nodeID = e.getKey();
            int[] propsIDs = e.getValue();
            int n = propsIDs.length / 2;
            for (int i = 0; i < n; i++) {
                graphConstructing.setNodeProperty(
                        provider.getString(nodeID),
                        provider.getString(propsIDs[2 * i]),
                        provider.getString(propsIDs[2 * i + 1]));
            }
        }
    }

    public Node getNode(String id) {
        int stringId = provider.getStringId(id);
        if (!nodeIds.contains(stringId)) {
            throw new NoSuchElementException();
        }
        return new MyNode(stringId);
    }

    public boolean hasNode(String id) {
        int stringId = provider.getStringIdOrZero(id);
        return stringId != 0 && nodeIds.contains(stringId);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringGraphData that = (StringGraphData) o;
        return provider.equals(that.provider) && nodeIds.equals(that.nodeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, nodeIds);
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

    public Nodes fromNodeOfEdgesWithLabel(Edges edges, String edgeLabel) {
        int edgesSize = edges.getSize();
        int labelId = provider.getStringIdOrZero(edgeLabel);
        if (labelId == 0 || edgesSize == 0) {
            return EMPTY_NODES;
        }
        int[] buffer = new int[edgesSize];
        int i = 0;
        for (int id : asMyEdges(edges).edgesOffsets) {
            if (provider.getLabelId(id) == labelId) {
                buffer[i++] = provider.getFromId(id);
            }
        }
        return new MyNodes(Arrays.copyOf(buffer, i));
    }

    private static int[] intArray(@Nullable Collection<Integer> integers) {
        if (integers == null) {
            return new int[0];
        }
        int n = integers.size();
        int[] result = new int[n];
        int i = 0;
        for (int x : integers) {
            result[i++] = x;
        }
        return result;
    }

    @Nullable
    private Nodes fromNodes;

    public Nodes fromNodes() {
        if (fromNodes == null) {
            fromNodes = new MyNodes(edgesIndexForFromNode.keys());
        }
        return fromNodes;
    }

    @Nullable
    private Nodes toNodes;

    public Nodes toNodes() {
        if (toNodes == null) {
            toNodes = new MyNodes(edgesIndexForToNode.keys());
        }
        return toNodes;
    }
}
