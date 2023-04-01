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
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NodesImpl implements Nodes {
    private final Set<Node> items;

    private NodesImpl(Set<Node> items) {
        this.items = items;
    }

    public static Nodes createNodes(Set<Node> items) {
        return new NodesImpl(items);
    }

    @Override
    public int getSize() {
        return items.size();
    }
    
    @Override
    public Stream<Node> stream() {
        return items.stream();
    }

    @Override
    public Nodes intersected(Nodes otherNodes) {
        if (otherNodes == this) {
            return this;
        }
        
        int nA = getSize();
        int nB = otherNodes.getSize();

        Set<Node> setOfNodes;
        Nodes nodesToCheck;
        if (nA >= nB) {
            setOfNodes = stream().collect(Collectors.toSet());
            nodesToCheck = otherNodes;
        } else {
            setOfNodes = otherNodes.stream().collect(Collectors.toSet());
            nodesToCheck = this;
        }
        Set<Node> result = nodesToCheck.stream()
                .filter(setOfNodes::contains)
                .collect(Collectors.toSet());
        return createNodes(result);
    }


    @Override
    public Iterator<Node> iterator() {
        return items.iterator();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodesImpl strings = (NodesImpl) o;
        return items.equals(strings.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
