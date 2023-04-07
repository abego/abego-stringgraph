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

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;

class NodesImpl implements Nodes {
    private final int[] nodesIDs;
    private final StringGraphState state;
    private boolean isSorted = false;

    static NodesImpl asNodesImpl(Nodes nodes) {
        if (!(nodes instanceof NodesImpl)) {
            throw new IllegalArgumentException("NodesImpl expected, got " + nodes.getClass());
        }
        return (NodesImpl) nodes;
    }

    NodesImpl(int[] nodesIDs, StringGraphState state) {
        this.nodesIDs = nodesIDs;
        this.state = state;
    }

    @Override
    public int getSize() {
        return nodesIDs.length;
    }

    @Override
    public Stream<Node> stream() {
        return Arrays.stream(nodesIDs)
                .mapToObj(id -> new NodeImpl(id, state));
    }

    @Override
    public Nodes intersected(Nodes otherNodes) {
        if (otherNodes == this) {
            return this;
        }

        // sort both id arrays
        int[] a = sortedIds();
        int[] b = asNodesImpl(otherNodes).sortedIds();
        // compare the items of both id arrays and add all ids that are
        // in both array to the buffer array
        int nA = a.length;
        int nB = b.length;
        int maxResultSize = Math.min(nA, nB);
        int[] buffer = new int[maxResultSize];
        int iBuffer = 0;
        int iA = 0;
        int iB = 0;
        while (iA < nA && iB < nB) {
            int vA = a[iA];
            int vB = b[iB];
            if (vA == vB) {
                buffer[iBuffer++] = vA;
                iA++;
                iB++;
            } else if (vA > vB) {
                //noinspection StatementWithEmptyBody
                while (++iB < nB && vA > b[iB]) {
                    // empty by intend
                }
            } else {
                // vB > vA
                //noinspection StatementWithEmptyBody
                while (++iA < nA && vB > a[iA]) {
                    // empty by intend
                }
            }
        }
        return new NodesImpl(Arrays.copyOf(buffer, iBuffer), state);
    }

    private int[] sortedIds() {
        if (!isSorted) {
            Arrays.sort(nodesIDs);
            isSorted = true;
        }
        return nodesIDs;
    }

    @Override
    public Iterator<Node> iterator() {
        return stream().iterator();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodesImpl nodes = (NodesImpl) o;
        return Arrays.equals(nodesIDs, nodes.nodesIDs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(nodesIDs);
    }

}
