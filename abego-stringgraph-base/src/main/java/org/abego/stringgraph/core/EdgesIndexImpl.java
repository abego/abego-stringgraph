package org.abego.stringgraph.core;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

class EdgesIndexImpl<K> implements EdgesIndex<K> {
    private final Map<K, Set<Edge>> map;

    private EdgesIndexImpl(Map<K, Set<Edge>> map) {
        this.map = map;
    }

    public static <K> EdgesIndex<K> createEdgesIndex(Map<K, Set<Edge>> map) {
        return new EdgesIndexImpl<>(map);
    }

    @Override
    public int edgesCount(K key) {
        Set<Edge> edges = map.get(key);
        return edges == null ? 0 : edges.size();
    }

    @Override
    public Edges edges(K key) {
        Set<Edge> edges = map.get(key);
        return EdgesImpl.createEdges(
                edges == null ? Collections.emptySet() : edges);
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }
}
