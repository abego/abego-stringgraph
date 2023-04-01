package org.abego.stringgraph.core;
 
public interface EdgeFactory {
    Edge newEdge(String fromNode, String edgeLabel, String toNode);
}
