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

import org.abego.stringgraph.core.EdgeLabels;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;

class EdgeLabelsImpl implements EdgeLabels {
    public static final EdgeLabels EMPTY_EDGE_LABELS =
            createEdgeLabels(emptySet());
    
    private final Set<String> items;

    private EdgeLabelsImpl(Set<String> items) {
        this.items = items;
    }

    public static EdgeLabels createEdgeLabels(Set<String> items) {
        return new EdgeLabelsImpl(items);
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public Stream<String> stream() {
        return items.stream();
    }

    @Override
    public Iterator<String> iterator() {
        return items.iterator();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeLabelsImpl strings = (EdgeLabelsImpl) o;
        return items.equals(strings.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }
}
