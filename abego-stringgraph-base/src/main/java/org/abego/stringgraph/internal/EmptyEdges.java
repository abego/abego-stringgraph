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

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;
import static java.util.Collections.emptyList;

class EmptyEdges implements Edges {
    public static final Edges EMPTY_EDGES = new EmptyEdges();

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public Stream<Edge> stream() {
        return Stream.empty();
    }

    @Override
    public boolean contains(Edge edge) {
        return false;
    }

    @Override
    public boolean contains(String fromNode, String label, String toNode) {
        return false;
    }

    @Override
    public Edges filtered(Predicate<Edge> edgePredicate) {
        return this;
    }

    @Override
    public Edges intersected(Edges otherEdges) {
        return this;
    }

    @Override
    public Iterable<Edge> sorted(Comparator<? super Edge> comparator) {
        return emptyList();
    }

    @Override
    public Iterable<Edge> sorted() {
        return emptyList();
    }

    @Override
    public Iterator<Edge> iterator() {
        return emptyIterator();
    }
}
