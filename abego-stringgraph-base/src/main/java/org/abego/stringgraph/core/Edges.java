package org.abego.stringgraph.core;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Edges extends Iterable<Edge> {
    int getSize();

    Stream<Edge> stream();

    boolean contains(Edge edge);

    boolean contains(String fromNode, String label, String toNode);


    Edges filtered(Predicate<Edge> edgePredicate);

    Edges intersected(Edges otherEdges);

    //TODO make this return Edges
    Iterable<Edge> sorted(Comparator<? super Edge> comparator);

    //TODO make this return Edges
    Iterable<Edge> sorted();

    default Iterable<String> sortedEdgesTexts() {
        return stream().sorted().map(Edge::getText)
                .collect(Collectors.toList());
    }
}
