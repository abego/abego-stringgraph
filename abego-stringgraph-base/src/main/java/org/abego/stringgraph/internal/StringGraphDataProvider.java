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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Provides "raw" data of a StringGraph, mainly using {@code int} values to
 * identify nodes, edges, labels etc. .
 * <p>
 * Instead of working directly on {@code String} objects most methods use 
 * {@code int} values ('id') to identify the string value (and hence the Node, 
 * Edge label, Property name or value). The string value for an {@code int} can 
 * then be retrieved through the {@link #getString(int)} method. For a given
 * String the method {@link #getStringId(String)} returns its {@code int} value/
 * id.
 */
public interface StringGraphDataProvider {
    /**
     * Returns the (numeric) ids of all nodes. 
     */
    int[] getNodesIds();

    /**
     * Returns the (numeric) id of the 'from'-node of the edge with the given 
     * {@code edgeId}. 
     */
    int getFromId(int edgeId);

    /**
     * Returns the (numeric) id of the 'to'-node of the edge with the given 
     * {@code edgeId}. 
     */
    int getToId(int edgeId);

    /**
     * Returns the (numeric) id of the label of the edge with the given 
     * {@code edgeId}. 
     */
    int getLabelId(int edgeId);

    /**
     * Returns the number of edges.
     */
    int getEdgesCount();

    /**
     * Returns the properties of the node with the given {@code nodeId} as a 
     * sequence of key-value pairs, each key-value pair holding the string id
     * of the corresponding key/value, or return {@code null} when the node 
     * does not exist or has properties.
     */
    int @Nullable [] getPropertyDataForNode(int nodeId);

    /**
     * Returns the ids of all Nodes that have PropertyData (/Properties).
     */
    int[] getNodesWithProperties();

    /**
     * Returns the String associated with the given {@code id}, as used as 
     * a node's id, an edge's label and the properties' keys and values.
     */
    String getString(int id);

    /**
     * Returns the (numeric) id associated with the given {@code string} or 
     * throws a {@link java.util.NoSuchElementException} when no id is 
     * associated with that string.
     */
    int getStringId(String string);
    
    /**
     * Returns the (numeric) id associated with the given {@code string} or 
     * {@code 0} when no id is associated with that string.
     */
    int getStringIdOrZero(String string);
}
