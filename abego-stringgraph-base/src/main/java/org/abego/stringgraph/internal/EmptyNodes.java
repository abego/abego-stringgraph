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

import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.core.Nodes;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyIterator;

class EmptyNodes implements Nodes {
    static final Nodes EMPTY_NODES = new EmptyNodes();

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public Stream<Node> stream() {
        return Stream.empty();
    }

    @Override
    public Nodes intersected(Nodes otherNodes) {
        return this;
    }

    @Override
    public Nodes union(Nodes otherNodes) {
        return otherNodes;
    }

    @Override
    public Nodes filter(Predicate<Node> predicate) {
        return this;
    }

    @Override
    public Iterator<Node> iterator() {
        return emptyIterator();
    }
}
