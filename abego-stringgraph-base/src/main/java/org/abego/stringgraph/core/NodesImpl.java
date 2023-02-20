package org.abego.stringgraph.core;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

class NodesImpl implements Nodes {
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
