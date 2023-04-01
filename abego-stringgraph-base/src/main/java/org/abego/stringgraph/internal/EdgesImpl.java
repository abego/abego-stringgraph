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
import org.eclipse.jdt.annotation.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

public final class EdgesImpl implements Edges {
    private static final Edges EMPTY_EDGES = new EdgesImpl(emptySet());
    private final Set<Edge> items;

    private EdgesImpl(Set<Edge> items) {
        this.items = items;
    }

    static Edges createEdges(Set<Edge> items) {
        return items.isEmpty() ? emptyEdges() : new EdgesImpl(items);
    }
    
    static Edges emptyEdges() {
        return EMPTY_EDGES;
    }

    @Override
    public Iterator<Edge> iterator() {
        return items.iterator();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgesImpl otherEdges = (EdgesImpl) o;
        return items.equals(otherEdges.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public boolean contains(Edge edge) {
        return items.contains(edge);
    }

    @Override
    public boolean contains(String fromNode, String label, String toNode) {
        return items.stream().anyMatch(e->
                e.getFromNode().id().equals(fromNode) &&
                        e.getToNode().id().equals(toNode) &&
                        e.getLabel().equals(label));
    }

    @Override
    public Edges filtered(Predicate<Edge> edgePredicate) {
        return createEdges(stream().filter(edgePredicate)
                .collect(Collectors.toSet()));
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
    public Stream<Edge> stream() {
        return items.stream();
    }

    public Edges intersected(Edges otherEdges) {
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
        for(Edge e:fewerEdges) {
            if (moreEdges.contains(e)) {
                result.add(e);
            }
        }
        
        return createEdges(result);
    }
}
