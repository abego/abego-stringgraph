package org.abego.stringgraph.core;

import java.util.stream.Stream;

public interface Nodes extends Iterable<Node> {
    int getSize();
    
    Stream<Node> stream();
}
