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
import org.abego.stringgraph.core.EdgeFactory;
import org.abego.stringgraph.core.EdgeLabels;
import org.abego.stringgraph.core.Edges;
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;
import org.abego.stringgraph.core.Properties;
import org.abego.stringgraph.core.Property;
import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphException;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StringGraphImpl implements StringGraph {
    private final Nodes nodes;
    private final Edges edges;
    private final Function<String, Properties> nodeProperties;
    private final EdgeFactory edgeFactory;

    /**
     * Links every fromNode to the Edges it belongs to.
     */
    private final EdgesIndex<Node> edgesIndexForFromNode;
    /**
     * Links every toNode to the Edges it belongs to.
     */
    private final EdgesIndex<Node> edgesIndexForToNode;
    /**
     * Links every label to the Edges it belongs to.
     */
    private final EdgesIndex<String> edgesIndexForLabel;

    private StringGraphImpl(Nodes nodes,
                            Edges edges,
                            Function<String, Properties> nodeProperties,
                            EdgeFactory edgeFactory) {
        this.nodes = nodes;
        this.edges = edges;
        this.nodeProperties = nodeProperties;
        this.edgeFactory = edgeFactory;

        EdgesIndexBuilder<Node> edgesIndexForFromNodeBuilder = EdgesIndexBuilder.createEdgesIndexBuilder();
        EdgesIndexBuilder<Node> edgesIndexForToNodeBuilder = EdgesIndexBuilder.createEdgesIndexBuilder();
        EdgesIndexBuilder<String> edgesIndexForLabelBuilder = EdgesIndexBuilder.createEdgesIndexBuilder();
        for (Edge e : edges) {
            edgesIndexForFromNodeBuilder.add(e.getFromNode(), e);
            edgesIndexForToNodeBuilder.add(e.getToNode(), e);
            edgesIndexForLabelBuilder.add(e.getLabel(), e);
        }
        edgesIndexForFromNode = edgesIndexForFromNodeBuilder.build();
        edgesIndexForToNode = edgesIndexForToNodeBuilder.build();
        edgesIndexForLabel = edgesIndexForLabelBuilder.build();
    }

    public static StringGraph createStringGraph(
            Nodes nodes,
            Edges edges,
            Function<String, Properties> nodeProperties,
            EdgeFactory edgeFactory) {
        return new StringGraphImpl(nodes, edges, nodeProperties, edgeFactory);
    }

    @Override
    public Nodes fromNodes() {
        return NodesImpl.createNodes(edgesIndexForFromNode.keySet());
    }

    @Override
    public Nodes nodes() {
        return nodes;
    }

    private enum PatternKind {
        QUERY,
        BOUND,
        NULL,
    }

    private static PatternKind patternKind(@Nullable String pattern) {
        return pattern == null ? PatternKind.NULL :
                pattern.startsWith("?") ? PatternKind.QUERY :
                        PatternKind.BOUND;
    }

    @Override
    @SuppressWarnings({"PointlessArithmeticExpression", "DataFlowIssue"})
    public Nodes nodes(@Nullable String fromPattern,
                       @Nullable String labelPattern,
                       @Nullable String toPattern) {

        PatternKind fromKind = patternKind(fromPattern);
        PatternKind labelKind = patternKind(labelPattern);
        PatternKind toKind = patternKind(toPattern);

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
                return NodesImpl.createNodes(
                        edgesIndexForLabel.edges(labelPattern).stream()
                                .map(Edge::getFromNode)
                                .collect(Collectors.toSet()));
            case 2 + 3 * 1 + 9 * 0: // null, "lab", "?"
                return NodesImpl.createNodes(
                        edgesIndexForLabel.edges(labelPattern).stream()
                                .map(Edge::getToNode)
                                .collect(Collectors.toSet()));
            case 0 + 3 * 1 + 9 * 0: // "?", "lab", "?"
                Set<Node> nodes = new HashSet<>();
                edgesIndexForLabel.edges(labelPattern).forEach(edge -> {
                    nodes.add(edge.getFromNode());
                    nodes.add(edge.getToNode());
                });
                return NodesImpl.createNodes(nodes);
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
        return edges;
    }

    @Override
    public Edges edges(@Nullable String from, @Nullable String label, @Nullable String to) {
        List<Edges> edgesByPart = new ArrayList<>();
        if (from != null) {
            edgesByPart.add(edgesFromNode(from));
        }
        if (label != null) {
            edgesByPart.add(edgesLabeled(label));
        }
        if (to != null) {
            edgesByPart.add(edgesToNode(to));
        }
        
        if (edgesByPart.isEmpty()) {
            // no restriction -> all edges
            return edges();
        }
        
        if (edgesByPart.size() == 1) {
            // one restricting part -> use those edges
            return edgesByPart.get(0);
        }
        
        if (edgesByPart.size() == 3) {
            // all parts restricted -> exactly one edge can be selected, if it exists
            Edge candidate = edgeFactory.newEdge(from, label, to);
            return edges.contains(candidate) 
                    ? EdgesImpl.createEdges(candidate) : EdgesImpl.emptyEdges();
        }
        
        // we have exactly 2 Sets for the parts. Return the intersection of both
        return edgesByPart.get(0).intersected(edgesByPart.get(1));
    }

    @Override
    public Properties getNodeProperties(String node) {
        return nodeProperties.apply(node);
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
        return NodesImpl.createNodes(edgesIndexForToNode.keySet());
    }

    @Override
    public EdgeLabels edgeLabels() {
        return EdgeLabelsImpl.createEdgeLabels(edgesIndexForLabel.keySet());
    }

    @Override
    public Nodes nodesFromNode(String fromNode) {
        return NodesImpl.createNodes(
                edgesIndexForFromNode.edges(asNode(fromNode)).stream()
                        .map(Edge::getToNode)
                        .collect(Collectors.toSet()));
    }

    @Override
    public EdgeLabels edgeLabelsFromNode(String fromNode) {
        return EdgeLabelsImpl.createEdgeLabels(
                edgesIndexForFromNode.edges(asNode(fromNode)).stream()
                        .map(Edge::getLabel)
                        .collect(Collectors.toSet()));
    }

    @Override
    public Nodes nodesFromNodeViaEdgeLabeled(
            String fromNode, String edgeLabel) {
        return NodesImpl.createNodes(
                edgesIndexForFromNode.edges(asNode(fromNode)).stream()
                        .filter(e -> e.getLabel().equals(edgeLabel))
                        .map(Edge::getToNode)
                        .collect(Collectors.toSet()));
    }

    @Override
    public Nodes nodesToNode(String toNode) {
        return NodesImpl.createNodes(
                edgesIndexForToNode.edges(asNode(toNode)).stream()
                        .map(Edge::getFromNode)
                        .collect(Collectors.toSet()));
    }

    @Override
    public EdgeLabels edgeLabelsToNode(String toNode) {
        return EdgeLabelsImpl.createEdgeLabels(
                edgesIndexForToNode.edges(asNode(toNode)).stream()
                        .map(Edge::getLabel)
                        .collect(Collectors.toSet()));
    }

    @Override
    public Nodes nodesViaEdgeLabeledToNode(String edgeLabel, String toNode) {
        return NodesImpl.createNodes(
                edgesIndexForToNode.edges(asNode(toNode)).stream()
                        .filter(e -> e.getLabel().equals(edgeLabel))
                        .map(Edge::getFromNode)
                        .collect(Collectors.toSet()));
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
        return edgesIndexForFromNode.edges(asNode(fromNode));
    }

    @Override
    public Edges edgesToNode(String toNode) {
        return edgesIndexForToNode.edges(asNode(toNode));
    }

    @Override
    public boolean hasEdge(String fromNode, String edgeLabel, String toNode) {
        return edges.contains(edgeFactory.newEdge(fromNode, edgeLabel, toNode));
    }

    @Override
    public String toString() {
        return "StringGraphImpl{" + "nodes=" + nodes + ", edges=" + edges + '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringGraphImpl that = (StringGraphImpl) o;
        return nodes.equals(that.nodes) && edges.equals(that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges);
    }

    static Node asNode(String id) {
        return NodeImpl.createNode(id);
    }


}
