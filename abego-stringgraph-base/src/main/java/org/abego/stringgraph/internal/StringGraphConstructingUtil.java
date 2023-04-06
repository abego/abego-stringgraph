/*
 * MIT License
 *
 * Copyright (c) 2023 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.internal;

import org.abego.stringgraph.core.StringGraphConstructing;

public class StringGraphConstructingUtil {
    
    public static void constructGraph(
            StringGraphState state,
            StringGraphConstructing graphConstructing) {
        
        for (int nodesID : state.getNodesIds()) {
            graphConstructing.addNode(state.getString(nodesID));
        }

        int edgesCount = state.getEdgesCount();
        for (int i = 0; i < edgesCount; i++) {
            graphConstructing.addEdge(
                    state.getString(state.getFromId(i)),
                    state.getString(state.getLabelId(i)),
                    state.getString(state.getToId(i))
            );
        }

        for (int nodeID : state.getNodesWithProperties()) {
            int[] propsIDs = state.getPropertyDataForNode(nodeID);
            int n = propsIDs != null ? propsIDs.length / 2 : 0;
            for (int i = 0; i < n; i++) {
                graphConstructing.setNodeProperty(
                        state.getString(nodeID),
                        state.getString(propsIDs[2 * i]),
                        state.getString(propsIDs[2 * i + 1]));
            }
        }
    }

}
