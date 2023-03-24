package org.abego.stringgraph.core;

import java.util.stream.Stream;

public interface Nodes extends Iterable<Node> {
    
    /**
     * Returns the number of Node instances contained in this {@link Nodes} object.
     */
    int getSize();

    /**
     * Returns true when this {@link Nodes} object contains exactly on 
     * {@link Node} instance, otherwise false.
     */
    boolean hasSingleItem();

    /**
     * Returns the single Node object contained in this {@link Nodes} object or,
     * throws an {@link ExactlyOneNodeExpectedException} when the Nodes object 
     * contains no Node or more than one Node.
     */
    Node singleItem();

    /**
     * Returns the id of the single Node object contained in this {@link Nodes} 
     * object or, throws an {@link ExactlyOneNodeExpectedException} when the 
     * Nodes object contains no Node or more than one Node.
     */
    String singleItemId();

    /**
     * Returns a stream of the Node instances contained in this {@link Nodes}
     * object.
     */
    Stream<Node> stream();
}
