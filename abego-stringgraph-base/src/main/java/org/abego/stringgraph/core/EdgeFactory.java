package org.abego.stringgraph.core;
 
interface EdgeFactory {
    Edge newEdge(String fromNode, String edgeLabel, String toNode);
}
