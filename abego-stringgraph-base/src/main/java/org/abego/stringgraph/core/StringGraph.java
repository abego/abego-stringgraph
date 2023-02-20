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
     *     <li>a String to be used to limit the edges to considered to those
     *     that have the given value as for that part, or</li>
     *     <li>be {@code null} to allow any possible value for that part.</li>
     * </ul>
     * <p>
     * The following combinations are supported:
     * <table>
     *     <tr><th>Call</th><th>Result</th></tr>
     *     <tr><td>{@code nodes("?", null, null)}</td><td>all nodes used at the "from" side</td></tr>
     *     <tr><td>{@code nodes(null, null, "?")}</td><td> all nodes used at the "to" side</td></tr>
     *     <tr><td>{@code nodes("?", null, "?")}</td><td> all nodes</td></tr>
     *     <tr><td>{@code nodes("?", null, "ABC")}</td><td> all nodes that reference "ABC"</td></tr>
     *     <tr><td>{@code nodes("ABC", null, "?")}</td><td> all nodes that are referenced by "ABC"</td></tr>
     *     <tr><td>{@code nodes("?", "lab", "ABC")}</td><td> all nodes that reference "ABC" through an edge with label "lab"</td></tr>
     *     <tr><td>{@code nodes("ABC", "lab", "?")}</td><td> all nodes that are referenced by "ABC" through an edge with label "lab"</td></tr>
     *     <tr><td>{@code nodes("?", "lab", null)}</td><td> all nodes referencing something through an edge with label "lab"</td></tr>
     *     <tr><td>{@code nodes(null, "lab", "?")}</td><td> all nodes that are referenced through an edge with label "lab"</td></tr>
     *     <tr><td>{@code nodes("?", "lab", "?")}</td><td> all nodes used in edges with label "lab"</td></tr>
     * </table>
     * <p>
     */
    Nodes nodes(@Nullable String fromPattern, @Nullable String labelPattern, @Nullable String toPattern);

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

    Nodes fromNodes();

    Nodes toNodes();

    EdgeLabels edgeLabels();

    Nodes nodesFromNode(String fromNode);

    EdgeLabels edgeLabelsFromNode(String fromNode);

    Nodes nodesFromNodeViaEdgeLabeled(
            String fromNode, String edgeLabel);

    Nodes nodesToNode(String toNode);

    EdgeLabels edgeLabelsToNode(String toNode);

    Nodes nodesToNodeViaEdgeLabeled(String toNode, String edgeLabel);

    Edges edgesWith(Predicate<Edge> edgePredicate);

    Edges edgesLabeled(String edgeLabel);

    Edges edgesFromNode(String fromNode);

    Edges edgesToNode(String toNode);

    boolean hasEdge(String fromNode, String toNode, String edgeLabel);
}
