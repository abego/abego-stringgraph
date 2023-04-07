/*
 * MIT License
 *
 * Copyright (c) 2022-2023 Udo Borkowski, (ub@abego.org)
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

package org.abego.stringgraph.internal.commons;

import org.abego.stringgraph.core.Edge;

import java.util.Comparator;

import static org.abego.stringgraph.internal.commons.StringUtil.quotedIfNeeded;

public final class EdgeUtil {
    private static final Comparator<Edge> COMPARATOR = createComparator();
    private static final Comparator<Edge> COMPARATOR_LABEL_LAST = createComparatorLabelLast();

    EdgeUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the "default" Comparator for Edges, sorting them by
     * (fromNode, label, toNode).
     */
    public static Comparator<Edge> getComparator() {
        return COMPARATOR;
    }

    /**
     * Returns a Comparator for Edges, sorting them by (fromNode, toNode, label).
     */
    public static Comparator<Edge> getComparatorLabelLast() {
        return COMPARATOR_LABEL_LAST;
    }

    public static String calcEdgeText(Edge edge) {
        StringBuilder sb = new StringBuilder();
        sb.append(edge.getFromNode().getText());
        
        String label = edge.getLabel();
        if (!label.isEmpty()) {
            sb.append(" --");
            sb.append(quotedIfNeeded(label));
            sb.append("--> ");
        } else {
            sb.append(" --> ");
        }
        
        sb.append(edge.getToNode().getText());

        return sb.toString();
    }
    
    private static Comparator<Edge> createComparator() {
        return Comparator
                .comparing(Edge::getFromNode)
                .thenComparing(Edge::getLabel)
                .thenComparing(Edge::getToNode);
    }

    private static Comparator<Edge> createComparatorLabelLast() {
        return Comparator
                .comparing(Edge::getFromNode)
                .thenComparing(Edge::getToNode)
                .thenComparing(Edge::getLabel);
    }
}
