/*
 * MIT License
 *
 * Copyright (c) 2022 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.core;

import org.abego.stringgraph.internal.StringUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Comparator;
import java.util.Objects;

import static org.abego.stringgraph.core.StringGraphImpl.asNode;

final class EdgeImpl implements Edge {
    private static final Comparator<Edge> COMPARATOR = EdgeUtil.getComparator();
    private final Node fromNode;
    private final String label;
    private final Node toNode;

    private EdgeImpl(Node fromNode, String label, Node toNode) {
        this.fromNode = fromNode;
        this.label = label;
        this.toNode = toNode;
    }

    static Edge createEdge(Node fromNode, String label, Node toNode) {
        return new EdgeImpl(fromNode, label, toNode);
    }
    
    static Edge createEdge(String fromNode, String label, String toNode) {
        return new EdgeImpl(asNode(fromNode), label, asNode(toNode));
    }

    @Override
    public Node getFromNode() {
        return fromNode;
    }
    
    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Node getToNode() {
        return toNode;
    }

    @Override
    public String getText() {
        return EdgeUtil.calcEdgeText(this);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeImpl edge = (EdgeImpl) o;
        //noinspection CallToSuspiciousStringMethod
        return getFromNode().equals(edge.getFromNode())
                && getLabel().equals(edge.getLabel())
                && getToNode().equals(edge.getToNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFromNode(), getLabel(), getToNode());
    }

    @Override
    public String toString() {
        //noinspection HardCodedStringLiteral,StringConcatenation,MagicCharacter
        return "EdgeDefault{" +
                "fromNode=" + StringUtil.quoted2(fromNode.id()) +
                ", label=" + StringUtil.quoted2(label) +
                ", toNode=" + StringUtil.quoted2(toNode.id()) +
                '}';
    }

    @Override
    public int compareTo(Edge o) {
        return COMPARATOR.compare(this, o);
    }
}
