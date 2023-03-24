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
    default boolean hasSingleItem() {
        return getSize() == 1;
    }

    /**
     * Returns the single Node object contained in this {@link Nodes} object or,
     * throws an {@link ExactlyOneNodeExpectedException} when the Nodes object 
     * contains no Node or more than one Node.
     */
    default Node singleItem() {
        if (!hasSingleItem()) {
            throw new ExactlyOneNodeExpectedException(getSize());
        }
        return iterator().next();
    }
    
    /**
     * Returns the id of the single Node object contained in this {@link Nodes} 
     * object or, throws an {@link ExactlyOneNodeExpectedException} when the 
     * Nodes object contains no Node or more than one Node.
     */
    default String singleItemId() {
        return singleItem().id();
    }

    /**
     * Returns a stream of the Node instances contained in this {@link Nodes}
     * object.
     */
    Stream<Node> stream();
    
    /**
     * Returns a stream of the id's of the Node instances contained in this 
     * {@link Nodes} object.
     */
    default Stream<String> idStream() {
        return stream().map(Node::id);
    }

    /**
     * Returns a Nodes object with the intersection of this Nodes object and
     * the otherNodes object, i.e. with all Node instances that are in both.
     * <p>
     * This object and the otherNodes are not modified.
     */
    Nodes intersectedWith(Nodes otherNodes);
}
