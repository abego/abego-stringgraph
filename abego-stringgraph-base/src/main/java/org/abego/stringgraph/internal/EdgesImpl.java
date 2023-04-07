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
import org.abego.stringgraph.internal.commons.EdgeUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.abego.stringgraph.internal.EdgeImpl.asEdgeImpl;
import static org.abego.stringgraph.internal.commons.ClassUtil.className;

class EdgesImpl implements Edges {
    private final int[] edgesIds;
    private final StringGraphState state;

    static EdgesImpl asEdgesImpl(@Nullable Edges edges) {
        if (!(edges instanceof EdgesImpl)) {
            throw new IllegalArgumentException("EdgesImpl expected, got " + className(edges));
        }
        return (EdgesImpl) edges;
    }

    EdgesImpl(int[] edgesIds, StringGraphState state) {
        this.edgesIds = edgesIds;
        this.state = state;
    }

    @Override
    public int getSize() {
        return edgesIds.length;
    }

    @Override
    public boolean contains(Edge edge) {
        int id = asEdgeImpl(edge).idAsInt();
        //TODO: more efficient implementation?
        for (int edgesOffset : edgesIds) {
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
        for (int edgesOffset : edgesIds) {
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
        return Arrays.stream(edgesIds)
                .mapToObj(edgesOffset -> new EdgeImpl(edgesOffset, state));
    }

    @Override
    public Edges filtered(Predicate<Edge> edgePredicate) {
        return createEdges(stream().filter(edgePredicate)
                .collect(Collectors.toSet()));
    }

    private Edges createEdges(Set<Edge> edges) {
        int n = edges.size();
        int[] resultIds = new int[n];
        int i = 0;
        for (Edge edge : edges) {
            resultIds[i++] = asEdgeImpl(edge).idAsInt();
        }
        return new EdgesImpl(resultIds, state);
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

        return createEdges(result);
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
        EdgesImpl edges = (EdgesImpl) o;
        return Arrays.equals(edgesIds, edges.edgesIds);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(edgesIds);
    }

    int[] edgesIds() {
        return edgesIds;
    }
}
