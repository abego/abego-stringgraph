package org.abego.stringgraph.core;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Objects;

class NodeImpl implements Node {
    private final String id;

    private NodeImpl(String id) {
        this.id = id;
    }

    public static Node createNode(String id) {
        return new NodeImpl(id);
    }

    @Override
    public String id() {
        return id;
    }


    @Override
    public int compareTo(Node o) {
        return id.compareTo(o.id());
    }
    
    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeImpl node = (NodeImpl) o;
        return id.equals(node.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NodeImpl{" +
                "id='" + id + '\'' +
                '}';
    }
}
