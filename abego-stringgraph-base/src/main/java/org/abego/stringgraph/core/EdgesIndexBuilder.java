package org.abego.stringgraph.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class EdgesIndexBuilder<K> {
    private final Map<K, Set<Edge>> map = new HashMap<>();
    private boolean building = true;

    private EdgesIndexBuilder() {
    }

    public static <K> EdgesIndexBuilder<K> createEdgesIndexBuilder() {
        return new EdgesIndexBuilder<>();
    }

    public void add(K key, Edge edge) {
        map.computeIfAbsent(key, k -> new HashSet<>()).add(edge);
    }

    public EdgesIndex<K> build() {
        if (!building) {
            throw new IllegalStateException("Must call `build()` only once.");
        }
        building = false;
        return EdgesIndexImpl.createEdgesIndex(map);
    }
}
