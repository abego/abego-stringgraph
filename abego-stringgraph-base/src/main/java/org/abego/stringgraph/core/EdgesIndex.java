package org.abego.stringgraph.core;

import java.util.Set;

interface EdgesIndex<K> {
    int edgesCount(K key);

    Edges edges(K key);

    Set<K> keySet();
}
