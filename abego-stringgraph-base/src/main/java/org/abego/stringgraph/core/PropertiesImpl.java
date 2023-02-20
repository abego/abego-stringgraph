/*
 * MIT License
 *
 * Copyright (c) 2022 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.stringgraph.core;

import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

final class PropertiesImpl implements Properties {
    private final Map<String, String> propertyMap;
    @Nullable
    private List<String> propertyNames;

    public static final Properties EMPTY_PROPERTIES = createProperties(new HashMap<>());

    private PropertiesImpl(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }

    static Properties createProperties(Map<String, String> propertyMap) {
        return new PropertiesImpl(propertyMap);
    }

    @Override
    public int getSize() {
        return propertyMap.size();
    }

    @Override
    public Stream<Property> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public Iterator<Property> iterator() {
        return new Iterator<Property>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < getSize();
            }

            @Override
            public Property next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return item(i++);
            }
        };
    }

    @Override
    @Nullable
    public Property getPropertyOrNull(String name) {
        return propertyMap.containsKey(name) ? createProperty(name) : null;
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
    public boolean hasProperty(String name) {
        return propertyMap.containsKey(name);
    }

    @Override
    public String getValueOfProperty(String name) {
        return propertyMap.get(name);
    }

    @Override
    public String getValueOfPropertyOrElse(String name, String defaultValue) {
        Property p = getPropertyOrNull(name);
        return p != null ? p.getValue() : defaultValue;
    }

    private Property item(int index) {
        if (propertyNames == null) {
            // make sure we have a well-defined order (sorted by key name)
            propertyNames = propertyMap.keySet().stream().sorted()
                    .collect(Collectors.toList());
        }
        return createProperty(propertyNames.get(index));
    }

    private Property createProperty(String name) {
        return PropertyImpl.createProperty(name, propertyMap.get(name));
    }

}
