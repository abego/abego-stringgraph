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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.abego.stringgraph.core.StringGraphImpl.asNode;

final class StringGraphBuilderImpl implements StringGraphBuilder, EdgeFactory {
    private final Set<Node> nodes = new HashSet<>();
    private final Set<Edge> edges = new HashSet<>();
    private final Map<String, Map<String, String>> nodeProperties = new HashMap<>();

    private StringGraphBuilderImpl() {
    }

    static StringGraphBuilder createStringGraphBuilder() {
        return new StringGraphBuilderImpl();
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    Map<String, Map<String, String>> getNodePropertiesMap() {
        return nodeProperties;
    }

    @Override
    public void addNode(String node) {
        nodes.add(asNode(node));
    }

    @Override
    public void addEdge(String fromNode, String edgeLabel, String toNode) {
        addNode(fromNode);
        addNode(toNode);
        edges.add(newEdge(fromNode, edgeLabel, toNode));
    }

    @Override
    public void setNodeProperty(String node, String name, String value) {
        if (!nodes.contains(asNode(node))) {
            throw new StringGraphException(String.format(
                    "Error when setting node property. Node does not exist: %s", node));
        }
        nodeProperties.computeIfAbsent(node, k -> new HashMap<>())
                .put(name, value);
    }

    @Override
    public StringGraph build() {
        return StringGraphImpl.createStringGraph(
                NodesImpl.createNodes(getNodes()),
                EdgesImpl.createEdges(getEdges()),
                getNodePropertiesMap(), 
                this);
    }

    @Override
    public Edge newEdge(String fromNode, String edgeLabel, String toNode) {
        return EdgeImpl.createEdge(asNode(fromNode), edgeLabel, asNode(toNode));
    }

}
