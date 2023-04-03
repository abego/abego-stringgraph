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
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.abego.stringgraph.internal.EmptyEdges.EMPTY_EDGES;

public class StringGraphImpl implements StringGraph {

    private final StringGraphData data;

    private StringGraphImpl(StringGraphData data) {
        this.data = data;
    }

    public static StringGraph createStringGraph(StringGraphData data) {
        return new StringGraphImpl(data);
    }

    @Override
    public Nodes fromNodes() {
        return data.fromNodes();
    }

    @Override
    public Nodes nodes() {
        return data.getNodes();
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
                return asNodes(
                        data.edgesIndexForLabel.edges(labelPattern).stream()
                                .map(Edge::getFromNode)
                                .collect(Collectors.toSet()));
            case 2 + 3 * 1 + 9 * 0: // null, "lab", "?"
                return asNodes(
                        data.edgesIndexForLabel.edges(labelPattern).stream()
                                .map(Edge::getToNode)
                                .collect(Collectors.toSet()));
            case 0 + 3 * 1 + 9 * 0: // "?", "lab", "?"
                Set<Node> nodes = new HashSet<>();
                data.edgesIndexForLabel.edges(labelPattern).forEach(edge -> {
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
        return data.getEdges();
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
        return data.getNodeProperties().apply(node);
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
        return data.toNodes();
    }

    @Override
    public EdgeLabels edgeLabels() {
        return EdgeLabelsImpl.createEdgeLabels(data.edgesIndexForLabel.keyStrings());
    }

    @Override
    public Nodes nodesFromNode(String fromNode) {
        return selectNodes(fromNode,
                data.edgesIndexForFromNode::edges,
                e -> true,
                Edge::getToNode);
    }

    @Override
    public EdgeLabels edgeLabelsFromNode(String fromNode) {
        return valueWithNodeOrElse(
                fromNode,
                n -> EdgeLabelsImpl.createEdgeLabels(
                        data.edgesIndexForFromNode.edges(n).stream()
                                .map(Edge::getLabel)
                                .collect(Collectors.toSet())),
                EdgeLabelsImpl.EMPTY_EDGE_LABELS);

    }

    @Override
    public Nodes nodesFromNodeViaEdgeLabeled(
            String fromNode, String edgeLabel) {
        return selectNodes(fromNode,
                data.edgesIndexForFromNode::edges,
                e -> e.getLabel().equals(edgeLabel),
                Edge::getToNode);
    }

    @Override
    public Nodes nodesToNode(String toNode) {
        return selectNodes(toNode,
                data.edgesIndexForToNode::edges,
                e -> true,
                Edge::getFromNode);
    }

    @Override
    public EdgeLabels edgeLabelsToNode(String toNode) {
        return valueWithNodeOrElse(
                toNode,
                n -> EdgeLabelsImpl.createEdgeLabels(
                        data.edgesIndexForToNode.edges(n).stream()
                                .map(Edge::getLabel)
                                .collect(Collectors.toSet())),
                EdgeLabelsImpl.EMPTY_EDGE_LABELS);

    }

    @Override
    public Nodes nodesViaEdgeLabeledToNode(String edgeLabel, String toNode) {
        try {
            Edges edges = data.edgesIndexForToNode.edges(toNode);
            return data.fromNodeOfEdgesWithLabel(edges, edgeLabel);
        } catch(NoSuchElementException e) {
            return EmptyNodes.EMPTY_NODES;
        }
    }

    @Override
    public Edges edgesWith(Predicate<Edge> edgePredicate) {
        return edges().filtered(edgePredicate);
    }

    @Override
    public Edges edgesLabeled(String edgeLabel) {
        return data.edgesIndexForLabel.edges(edgeLabel);
    }

    @Override
    public Edges edgesFromNode(String fromNode) {
        return valueWithNodeOrElse(
                fromNode, data.edgesIndexForFromNode::edges, EMPTY_EDGES);
    }

    @Override
    public Edges edgesToNode(String toNode) {
        return valueWithNodeOrElse(
                toNode, data.edgesIndexForToNode::edges, EMPTY_EDGES);
    }

    @Override
    public boolean hasEdge(String fromNode, String edgeLabel, String toNode) {
        return data.getEdges().contains(fromNode, edgeLabel, toNode);
    }

    @Override
    public String toString() {
        return "StringGraphImpl{" + "data=" + data + '}';
    }

    private Nodes asNodes(Set<Node> nodes) {
        return data.asNodes(nodes);
    }

    private Node asNode(String id) {
        return data.getNode(id);
    }

    private boolean hasNode(String nodeId) {
        return data.hasNode(nodeId);
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
        return hasNode(nodeId) ? function.apply(asNode(nodeId)) : elseValue;
    }
}
