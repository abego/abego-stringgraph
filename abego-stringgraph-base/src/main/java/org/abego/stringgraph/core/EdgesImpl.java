package org.abego.stringgraph.core;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.abego.commons.util.SetUtil.asSet;

final class EdgesImpl implements Edges {
    private static final Edges EMPTY_EDGES = new EdgesImpl(emptySet());
    private final Set<Edge> items;

    private EdgesImpl(Set<Edge> items) {
        this.items = items;
    }

    static Edges createEdges(Set<Edge> items) {
        return items.isEmpty() ? emptyEdges() : new EdgesImpl(items);
    }

    static Edges createEdges(Edge... items) {
        return items.length == 0 ? emptyEdges() : createEdges(asSet(items));
    }

    static Edges emptyEdges() {
        return EMPTY_EDGES;
    }

    @Override
    public Iterator<Edge> iterator() {
        return items.iterator();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgesImpl otherEdges = (EdgesImpl) o;
        return items.equals(otherEdges.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items);
    }

    @Override
    public int getSize() {
        return items.size();
    }

    @Override
    public boolean contains(Edge edge) {
        return items.contains(edge);
    }

    @Override
    public Edges filtered(Predicate<Edge> edgePredicate) {
        return createEdges(stream().filter(edgePredicate)
                .collect(Collectors.toSet()));
    }

    @Override
    public Iterable<Edge> sorted(Comparator<? super Edge> comparator) {
        return stream().sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public Iterable<Edge> sorted() {
        return sorted(EdgeUtil.getComparator());
    }

    @Override
    public Stream<Edge> stream() {
        return items.stream();
    }

    public Edges intersected(Edges otherEdges) {
        // find the Edges object with fewer items, to iterate over that object
        Edges fewerEdges;
        Edges moreEdges;
        if (getSize() < otherEdges.getSize()) {
            fewerEdges = this;
            moreEdges = otherEdges;
        } else {
            fewerEdges = otherEdges;
            moreEdges = this;
        }

        // All items in the fewerEdges also contains in moreEdges are included
        HashSet<Edge> result = new HashSet<>();
        for(Edge e:fewerEdges) {
            if (moreEdges.contains(e)) {
                result.add(e);
            }
        }
        
        return createEdges(result);
    }
}
