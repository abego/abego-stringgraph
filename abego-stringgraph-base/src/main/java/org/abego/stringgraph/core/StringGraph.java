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

import org.eclipse.jdt.annotation.Nullable;

import java.util.function.Predicate;

public interface StringGraph {

    //region Nodes

    /**
     * Returns all {@link Nodes} of this {@link StringGraph}
     */
    Nodes nodes();

    /**
     * Returns the {@link Nodes} according to the query given by the arguments.
     * <p>
     * Each of the arguments defines a pattern for the corresponding part
     * of an edge (from, label, to). It can either
     * <ul>
     *     <li>start with a {@code "?"} to indicate Nodes of this part are
     *     queried (should be returned when an edge is selected according
     *     to the query), or</li>
     *     <li>be a String not starting with {@code "?"} in which case only 
     *     those edges are considered that have the given string
     *     as their value that part, or</li>
     *     <li>be {@code null} to allow any possible value for that part.</li>
     * </ul>
     * <p>
     * The following combinations are supported:
     * <table>
     *     <tr><th>Call</th><th>Result</th></tr>
     *     <tr><td>{@code nodes("?", null, null)}</td><td>all nodes used at the "from" side (like {@link #fromNodes()})</td></tr>
     *     <tr><td>{@code nodes(null, null, "?")}</td><td> all nodes used at the "to" side (like {@link #toNodes()})</td></tr>
     *     <tr><td>{@code nodes("?", null, "?")}</td><td> all nodes (like {@link #nodes()})</td></tr>
     *     <tr><td>{@code nodes("?", null, "ABC")}</td><td> all nodes that reference "ABC" (like {@link #nodesToNode(String)})</td></tr>
     *     <tr><td>{@code nodes("ABC", null, "?")}</td><td> all nodes that are referenced by "ABC" (like {@link #nodesFromNode(String)})</td></tr>
     *     <tr><td>{@code nodes("?", "lab", "ABC")}</td><td> all nodes that reference "ABC" through an edge with label "lab" (like {@link #nodesViaEdgeLabeledToNode(String, String)})</td></tr>
     *     <tr><td>{@code nodes("ABC", "lab", "?")}</td><td> all nodes that are referenced by "ABC" through an edge with label "lab" (like {@link #nodesFromNodeViaEdgeLabeled(String, String)})</td></tr>
     *     <tr><td>{@code nodes("?", "lab", null)}</td><td> all nodes referencing something through an edge with label "lab"</td></tr>
     *     <tr><td>{@code nodes(null, "lab", "?")}</td><td> all nodes that are referenced through an edge with label "lab"</td></tr>
     *     <tr><td>{@code nodes("?", "lab", "?")}</td><td> all nodes used in edges with label "lab"</td></tr>
     * </table>
     * <p>
     */
    Nodes nodes(@Nullable String fromPattern, @Nullable String labelPattern, @Nullable String toPattern);

    Nodes fromNodes();

    Nodes toNodes();
    Nodes nodesFromNode(String fromNode);

    default Nodes nodesFrom(Node node) {
        return nodesFromNode(node.id());
    }

    Nodes nodesFromNodeViaEdgeLabeled(String fromNode, String edgeLabel);

    default Nodes nodesFromNodeViaEdgeLabeled(Node node, String edgeLabel) {
        return nodesFromNodeViaEdgeLabeled(node.id(), edgeLabel);
    }

    Nodes nodesToNode(String toNode);

    default Nodes nodesTo(Node node) {
        return nodesToNode(node.id());
    }

    Nodes nodesViaEdgeLabeledToNode(String edgeLabel, String toNode);

    default Nodes nodesViaEdgeLabeledTo(String edgeLabel, Node node) {
        return nodesViaEdgeLabeledToNode(edgeLabel, node.id());
    }
    //endregion
    
    //region Edges
    Edges edges();

    /**
     * Returns the {@link Edges} according to the query given by the arguments.
     * <p>
     * Each of the arguments defines a String required for the corresponding
     * part of an edge (from, label, to) to be selected. When the argument is
     * {@code null} the selection is not restricted for this part.
     * <p>
     * <b>Examples</b>
     * <table>
     *     <tr><th>Call</th><th>Result</th></tr>
     *     <tr><td>{@code edges("ABC", null, null)}</td><td>all edges from {@code "ABC"}</td></tr>
     *     <tr><td>{@code edges(null, "lab", null)}</td><td>all edges labeled "lab"</td></tr>
     *     <tr><td>{@code edges(null, null, "ABC")}</td><td>all edges to {@code "ABC"}</td></tr>
     *     <tr><td>{@code edges("ABC", "lab", null)}</td><td>all edges from {@code "ABC"} labeled "lab"</td></tr>
     *     <tr><td>{@code edges("ABC", "lab", "DEF")}</td><td>the edge from {@code "ABC"} to {@code "DEF"} labeled "lab"</td> (if it exists)</tr>
     *     <tr><td>{@code edges(null, null, null)}</td><td>all edges</td></tr>
     * </table>
     * <p>
     */
    Edges edges(@Nullable String from, @Nullable String label, @Nullable String to);


    Edges edgesWith(Predicate<Edge> edgePredicate);

    Edges edgesLabeled(String edgeLabel);

    Edges edgesFromNode(String fromNode);

    default Edges edgesFrom(Node node) {
        return edgesFromNode(node.id());
    }

    Edges edgesToNode(String toNode);

    default Edges edgesTo(Node node) {
        return edgesToNode(node.id());
    }

    boolean hasEdge(String fromNode, String edgeLabel, String toNode);

    default boolean hasEdge(Node fromNode, String edgeLabel, Node toNode) {
        return hasEdge(fromNode.id(), edgeLabel, toNode.id());
    }

    EdgeLabels edgeLabels();

    EdgeLabels edgeLabelsFromNode(String fromNode);

    default EdgeLabels edgeLabelsFrom(Node node) {
        return edgeLabelsFromNode(node.id());
    }
    EdgeLabels edgeLabelsToNode(String toNode);

    default EdgeLabels edgeLabelsTo(Node node) {
        return edgeLabelsToNode(node.id());
    }
    
    //endregion
    
    //region Properties
    Properties getNodeProperties(String node);

    default Properties getProperties(Node node) {
        return getNodeProperties(node.id());
    }

    boolean hasNodeProperty(String node, String propertyName);

    default boolean hasProperty(Node node, String propertyName) {
        return hasNodeProperty(node.id(), propertyName);
    }

    Property getNodeProperty(String node, String propertyName);

    default Property getProperty(Node node, String propertyName) {
        return getNodeProperty(node.id(), propertyName);
    }

    String getNodePropertyValue(String node, String propertyName);

    default String getPropertyValue(Node node, String propertyName) {
        return getNodePropertyValue(node.id(), propertyName);
    }

    String getNodePropertyValueOrElse(
            String node, String propertyName, String defaultValue);

    default String getPropertyValueOrElse(
            Node node, String propertyName, String defaultValue) {
        return getNodePropertyValueOrElse(node.id(), propertyName, defaultValue);
    }
    //endregion
}
