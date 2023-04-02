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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

class EdgesIndexImpl<K> implements EdgesIndex<K> {
    private final Map<K, Set<Edge>> map;
    private final Function<Set<Edge>,Edges> edgesFactory;

    private EdgesIndexImpl(Map<K, Set<Edge>> map, Function<Set<Edge>, Edges> edgesFactory) {
        this.map = map;
        this.edgesFactory = edgesFactory;
    }

    public static <K> EdgesIndex<K> createEdgesIndex(Map<K, Set<Edge>> map, Function<Set<Edge>, Edges> edgesFactory) {
        return new EdgesIndexImpl<>(map, edgesFactory);
    }

    @Override
    public int edgesCount(K key) {
        Set<Edge> edges = map.get(key);
        return edges == null ? 0 : edges.size();
    }

    @Override
    public Edges edges(K key) {
        Set<Edge> edges = map.get(key);
        return edgesFactory.apply(
                edges == null ? Collections.emptySet() : edges);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }
}
