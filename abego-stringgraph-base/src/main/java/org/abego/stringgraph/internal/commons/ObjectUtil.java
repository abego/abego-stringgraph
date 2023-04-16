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

package org.abego.stringgraph.internal.commons;

import org.eclipse.jdt.annotation.Nullable;

public class ObjectUtil {
    /**
     * Compares the "toString" representations of o1 and o2.
     * <p>
     * Comparison is done as in {@link String#compareToIgnoreCase(String)}, but
     * if the strings are equal when ignoring the case they are compared again
     * case sensitively to ensure a stable order between the two strings.
     * <p>
     * {@code null} values are assumed to be larger than any String.
     * <p>
     * This method does not take locale into account, and may result in an
     * unsatisfactory ordering for certain locales.
     */
    public static int compareAsTexts(@Nullable Object o1, @Nullable Object o2) {
        if (o1 == null) {
            return o2 == null ? 0 : 1;
        } else if (o2 == null) {
            return -1;
        }

        String s1 = o1.toString();
        String s2 = o2.toString();
        //noinspection CallToSuspiciousStringMethod
        int i = s1.compareToIgnoreCase(s2);
        //noinspection CallToSuspiciousStringMethod
        return i == 0 ? s1.compareTo(s2) : i;
    }
}
