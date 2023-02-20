package org.abego.stringgraph.core;

import java.util.stream.Stream;

public interface EdgeLabels extends Iterable<String> {
    int getSize();
    
    Stream<String> stream();
}
