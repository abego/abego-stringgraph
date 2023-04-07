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

import org.abego.stringgraph.core.Edges;
import org.abego.stringgraph.core.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.abego.stringgraph.internal.commons.ArrayUtil.intArray;
import static org.abego.stringgraph.internal.EmptyEdges.EMPTY_EDGES;

class EdgesIndex {
    private final StringGraphState state;
    private final Map<Integer, List<Integer>> map = new HashMap<>();
    private final Map<Integer, Edges> edgesMap = new HashMap<>();

    EdgesIndex(StringGraphState state) {
        this.state = state;
    }

    public void add(int key, int edgeId) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(edgeId);
    }

    public Edges edges(int key) {
        return key != 0
                ? edgesMap.computeIfAbsent(key, k -> new EdgesImpl(intArray(map.get(k)), state))
                : EMPTY_EDGES;
    }

    public Edges edges(Node node) {
        return edges(NodeImpl.asNodeImpl(node).idAsInt());
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
