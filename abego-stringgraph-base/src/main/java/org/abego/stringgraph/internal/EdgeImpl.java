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
import org.abego.stringgraph.core.Node;
import org.abego.stringgraph.internal.commons.EdgeUtil;
import org.eclipse.jdt.annotation.Nullable;

import static org.abego.stringgraph.internal.commons.EdgeUtil.calcEdgeText;
import static org.abego.stringgraph.internal.commons.StringUtil.quoted2;

class EdgeImpl implements Edge {
    private final int id;
    private final StringGraphState state;

    EdgeImpl(int id, StringGraphState state) {
        this.id = id;
        this.state = state;
    }

    static EdgeImpl asEdgeImpl(Edge edge) {
        if (!(edge instanceof EdgeImpl)) {
            throw new IllegalArgumentException("EdgeImpl expected, got " + edge.getClass());
        }
        return (EdgeImpl) edge;
    }

    @Override
    public Node getFromNode() {
        return new NodeImpl(state.getFromId(id), state);
    }

    @Override
    public String getLabel() {
        return state.getString(getLabelId());
    }

    private int getLabelId() {
        return state.getLabelId(id);
    }

    @Override
    public Node getToNode() {
        return new NodeImpl(state.getToId(id), state);
    }

    @Override
    public String getText() {
        return calcEdgeText(this);
    }

    @Override
    public int compareTo(Edge o) {
        return EdgeUtil.getComparator().compare(this, o);
    }

    @Override
    public String toString() {
        //noinspection HardCodedStringLiteral,StringConcatenation,MagicCharacter
        return "EdgeImpl{" +
                "fromNode=" + quoted2(getFromNode().id()) +
                ", label=" + quoted2(getLabel()) +
                ", toNode=" + quoted2(getToNode().id()) +
                "}";
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeImpl other = (EdgeImpl) o;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    int idAsInt() {
        return id;
    }
}
