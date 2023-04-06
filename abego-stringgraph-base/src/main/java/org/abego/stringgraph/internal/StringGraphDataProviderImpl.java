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

import org.abego.stringpool.StringPool;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import static org.abego.stringgraph.internal.ArrayUtil.intArray;

public class StringGraphDataProviderImpl implements StringGraphDataProvider {
    private final Map<Integer, int[]> props;
    private final int[] nodesIDs;
    private final int[] edgesIDs;
    private final StringPool strings;
    /**
     * Reading Strings from the StringPool is "slow" as the strings are encoded
     * every time. So cache the previously retrieved strings by their ID.
     */
    private final Map<Integer, String> cachedStrings = new HashMap<>();
    @Nullable
    private Map<String, Integer> stringIds;

    public StringGraphDataProviderImpl(Map<Integer, int[]> props, int[] nodesIDs, int[] edgesIDs, StringPool strings) {
        this.props = props;
        this.nodesIDs = nodesIDs;
        this.edgesIDs = edgesIDs;
        this.strings = strings;
    }

    @Override
    public int[] getNodesIds() {
        return nodesIDs;
    }

    @Override
    public int getFromId(int edgesOffset) {
        return edgesIDs[edgesOffset];
    }

    @Override
    public int getToId(int edgesOffset) {
        return edgesIDs[edgesOffset + 1];
    }

    @Override
    public int getLabelId(int edgesOffset) {
        return edgesIDs[edgesOffset + 2];
    }

    @Override
    public int getEdgesCount() {
        return edgesIDs.length / 3;
    }


    @Override
    public int @Nullable [] getPropertyDataForNode(int id) {
        return props.get(id);
    }

    @Override
    public int[] getNodesWithProperties() {
        return intArray(props.keySet());
    }

    @Override
    public String getString(int id) {
        return cachedStrings.computeIfAbsent(id, strings::getString);
    }

    @Override
    public int getStringId(String stringText) {
        int id = getStringIdOrZero(stringText);
        if (id == 0) {
            throw new NoSuchElementException();
        }
        return id;
    }

    @Override
    public int getStringIdOrZero(String stringText) {
        Integer result = getStringIds().get(stringText);
        return result != null ? result : 0;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringGraphDataProviderImpl that = (StringGraphDataProviderImpl) o;
        return props.equals(that.props) && Arrays.equals(nodesIDs, that.nodesIDs) && Arrays.equals(edgesIDs, that.edgesIDs) && strings.equals(that.strings);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(props, strings);
        result = 31 * result + Arrays.hashCode(nodesIDs);
        result = 31 * result + Arrays.hashCode(edgesIDs);
        return result;
    }

    private Map<String, Integer> getStringIds() {
        if (stringIds == null) {
            stringIds = new HashMap<>();
            for (StringPool.StringAndID e : strings.allStringAndIDs()) {
                stringIds.put(e.getString(), e.getID());
            }
        }
        return stringIds;
    }
}
