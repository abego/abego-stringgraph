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

import org.abego.stringgraph.core.exception.NoSuchPropertyException;
import org.abego.stringgraph.core.Properties;
import org.abego.stringgraph.core.Property;
import org.eclipse.jdt.annotation.Nullable;

import java.util.Iterator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class PropertiesImpl implements Properties {
    private final int[] keyValueIds;
    private final StringGraphState state;

    public PropertiesImpl(int[] keyValueIds, StringGraphState state) {
        this.keyValueIds = keyValueIds;
        this.state = state;
    }

    @Override
    public int getSize() {
        return keyValueIds.length / 2;
    }

    @Override
    public Stream<Property> stream() {
        return IntStream.range(0, getSize())
                .mapToObj(i -> new PropertyImpl(keyValueIds[2 * i], keyValueIds[2 * i + 1], state));
    }

    @Override
    public boolean hasProperty(String name) {
        int nameId = state.getStringIdOrZero(name);
        return nameId != 0 && IntStream.range(0, getSize())
                .anyMatch(i -> keyValueIds[2 * i] == nameId);
    }

    @Override
    public @Nullable Property getPropertyOrNull(String name) {
        int nameId = state.getStringId(name);
        return IntStream.range(0, getSize())
                .filter(i -> keyValueIds[2 * i] == nameId)
                .mapToObj(i -> new PropertyImpl(keyValueIds[2 * i], keyValueIds[2 * i + 1], state))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Property getProperty(String name) {
        Property property = getPropertyOrNull(name);
        if (property == null) {
            throw new NoSuchPropertyException(name);
        }
        return property;
    }

    @Override
    public String getValueOfProperty(String name) {
        return getProperty(name).getValue();
    }

    @Override
    public String getValueOfPropertyOrElse(String name, String defaultValue) {
        Property p = getPropertyOrNull(name);
        return p != null ? p.getValue() : defaultValue;
    }

    @Override
    public Iterator<Property> iterator() {
        return stream().iterator();
    }
}
