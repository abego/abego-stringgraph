/*
 * MIT License
 *
 * Copyright (c) 2022-2023 Udo Borkowski, (ub@abego.org)
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

/**
 * Indicates the version of the data format
 * <p>
 * With every release that changes the data format the
 * version numbers are changed according to the following rules,
 * assuming the previous version number was {@code m.n}:
 * <ul>
 *     <li>the new data format is incompatible with at least one
 *     release of this class with {@code majorNumber == m}:
 *     set {@code majorNumber} to {@code m + 1} and
 *     {@code minorNumber} to {@code 0}</li>
 *     <li>otherwise: leave {@code majorNumber} unchanged and
 *     set {@code minorNumber} to {@code n + 1}</li>
 * </ul>
 * This means that a release of this class with a {@code majorNumber == m}
 * can read files written by any release of this class that shares the
 * same {@code majorNumber == m}.
 */
class DataFormatVersion {
    public final int majorNumber;
    public final int minorNumber;

    private DataFormatVersion(int majorNumber, int minorNumber) {
        this.majorNumber = majorNumber;
        this.minorNumber = minorNumber;
    }

    public static DataFormatVersion createDataFormatVersion(
            int majorNumber, int minorNumber) {
        return new DataFormatVersion(majorNumber, minorNumber);
    }
}
