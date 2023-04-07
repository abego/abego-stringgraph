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
import org.eclipse.jdt.annotation.Nullable;

import static org.abego.stringgraph.internal.commons.ClassUtil.className;
import static org.abego.stringgraph.internal.commons.StringUtil.quoted2;
import static org.abego.stringgraph.internal.commons.StringUtil.quotedIfNeeded;

class NodeImpl implements Node {
    private final int id;
    private final StringGraphState state;

    static NodeImpl asNodeImpl(@Nullable Node node) {
        if (!(node instanceof NodeImpl)) {
            throw new IllegalArgumentException("NodeImpl expected, got " + className(node));
        }
        return (NodeImpl) node;
    }

    NodeImpl(int id, StringGraphState state) {
        this.id = id;
        this.state = state;
    }

    @Override
    public String id() {
        return state.getString(id);
    }

    @Override
    public String getText() {
        return quotedIfNeeded(id());
    }

    @Override
    public int compareTo(Node o) {
        // shortcut for the "equals" case.
        if (o instanceof NodeImpl) {
            if (((NodeImpl) o).id == id) {
                return 0;
            }
        }

        return id().compareTo(o.id());
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeImpl other = (NodeImpl) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "NodeImpl{" + "id=" + quoted2(id()) + "}";
    }

    int idAsInt() {
        return id;
    }
}
