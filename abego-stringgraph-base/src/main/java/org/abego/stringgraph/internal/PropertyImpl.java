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

import org.abego.stringgraph.core.Property;

import java.util.Comparator;

import static org.abego.stringgraph.internal.StringUtil.quoted2;

class PropertyImpl implements Property {
    private static final Comparator<Property> PROPERTY_COMPARATOR =
            Comparator.comparing(Property::getName)
                    .thenComparing(Property::getValue);
    private final int nameId;
    private final int valueId;
    private final StringGraphState state;

    public PropertyImpl(int nameId, int valueId, StringGraphState state) {
        this.nameId = nameId;
        this.valueId = valueId;
        this.state = state;
    }

    @Override
    public String getName() {
        return state.getString(nameId);
    }

    @Override
    public String getValue() {
        return state.getString(valueId);
    }

    @Override
    public String toString() {
        return "MyProperty{" +
                "name=" + quoted2(getName()) +
                ", value=" + quoted2(getValue()) +
                '}';
    }

    @Override
    public int compareTo(Property o) {
        return PROPERTY_COMPARATOR.compare(this, o);
    }
}
