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

import org.abego.stringgraph.core.StringGraph;
import org.abego.stringgraph.core.StringGraphBuilder;
import org.abego.stringgraph.core.exception.StringGraphException;
import org.abego.stringpool.StringPool;
import org.abego.stringpool.StringPoolBuilder;
import org.abego.stringpool.StringPools;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.abego.stringgraph.internal.commons.ArrayUtil.toIntArray;

final class StringGraphBuilderImpl implements StringGraphBuilder {
    private static class EdgeData {
        final int fromId;
        final int toId;
        final int labelId;

        EdgeData(int fromId, int toId, int labelId) {
            this.fromId = fromId;
            this.toId = toId;
            this.labelId = labelId;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EdgeData edgeData = (EdgeData) o;
            return fromId == edgeData.fromId && toId == edgeData.toId && labelId == edgeData.labelId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fromId, toId, labelId);
        }
    }

    private final StringPoolBuilder stringPoolBuilder = StringPools.builder();
    private final Set<Integer> nodes = new HashSet<>();
    private final Set<EdgeData> edges = new HashSet<>();
    private final Map<Integer, Map<Integer, Integer>> nodeProperties = new HashMap<>();

    private StringGraphBuilderImpl() {
    }

    public static StringGraphBuilder createStringGraphBuilder() {
        return new StringGraphBuilderImpl();
    }

    @Override
    public void addNode(String node) {
        nodes.add(stringPoolBuilder.add(node));
    }

    @Override
    public void addEdge(String fromNode, String edgeLabel, String toNode) {
        int fromId = stringPoolBuilder.add(fromNode);
        int toId = stringPoolBuilder.add(toNode);
        int labelId = stringPoolBuilder.add(edgeLabel);
        nodes.add(fromId);
        nodes.add(toId);
        edges.add(new EdgeData(fromId, toId, labelId));
    }

    @Override
    public void setNodeProperty(String node, String name, String value) {
        if (!stringPoolBuilder.contains(node)) {
            throw new StringGraphException(String.format(
                    "Error when setting node property. Node does not exist: %s", node));
        }
        nodeProperties.computeIfAbsent(stringPoolBuilder.add(node), k -> new HashMap<>())
                .put(stringPoolBuilder.add(name), stringPoolBuilder.add(value));
    }

    @Override
    public StringGraph build() {
        StringGraphState state = buildStringGraphState();
        return StringGraphImpl.createStringGraph(state);
    }

    public StringGraphState buildStringGraphState() {
        int[] nodesIds = toIntArray(nodes);
        int[] edgesIds = toFlatIntArray(edges);
        Map<Integer, int[]> props = toIntegerIntArrayMap(nodeProperties);
        StringPool strings = stringPoolBuilder.build();
        return new StringGraphStateImpl(props, nodesIds, edgesIds, strings);
    }

    private static int[] toFlatIntArray(Collection<EdgeData> edgeDataCollection) {
        int count = edgeDataCollection.size();
        int[] result = new int[count * 3];
        int offset = 0;
        for (EdgeData e : edgeDataCollection) {
            result[offset++] = e.fromId;
            result[offset++] = e.toId;
            result[offset++] = e.labelId;
        }
        return result;
    }

    private static Map<Integer, int[]> toIntegerIntArrayMap(
            Map<Integer, Map<Integer, Integer>> integerMapMap) {
        Map<Integer, int[]> props = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Integer>> e : integerMapMap.entrySet()) {
            Set<Map.Entry<Integer, Integer>> ps = e.getValue().entrySet();
            int[] array = new int[ps.size() * 2];
            int offset = 0;
            for (Map.Entry<Integer, Integer> e2 : ps) {
                array[offset++] = e2.getKey();
                array[offset++] = e2.getValue();
            }
            props.put(e.getKey(), array);
        }
        return props;
    }
}
