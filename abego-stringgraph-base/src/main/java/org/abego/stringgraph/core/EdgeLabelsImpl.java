package org.abego.stringgraph.core;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

class EdgeLabelsImpl implements EdgeLabels {
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
