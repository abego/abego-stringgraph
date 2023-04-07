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

import org.abego.stringgraph.internal.commons.StringUtil;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

// Original: https://github.com/abego/commons
public final class IterableUtil {
    private static final String EMPTY_TEXT_DEFAULT = "";

    private IterableUtil() {
    }

    public static int size(Iterable<?> iterable) {
        int result = 0;

        for (@SuppressWarnings("unused") Object o : iterable) {
            result++;
        }

        return result;
    }

    /**
     * Return the texts of the <code>items</code>, stringed together to a single
     * text.
     *
     * <p>Same as {@link IterableUtil#textOf(Iterable, CharSequence)} (preferred)</p>
     */
    public static <T> String join(CharSequence separator, Iterable<T> items) {
        return textOf(items, separator);
    }

    /**
     * Return the texts of the <code>items</code>, stringed together to a single
     * text.
     *
     * <p>Convenience overload of {@link #textOf(Iterable, CharSequence, String, Function, CharSequence)},
     * using default values for omitted parameter.</p>
     */
    public static <T> String textOf(
            Iterable<T> items,
            CharSequence separator) {
        return textOf(items, separator, EMPTY_TEXT_DEFAULT, Object::toString, StringUtil.NULL_STRING);
    }

    /**
     * Return the texts of the <code>items</code>, stringed together to a single
     * text.
     *
     * <p>Use <code>textOfItem(item)</code> as the text for an individual item.
     * When an item is <code>null</code> use the <code>nullValueText</code>.</p>
     *
     * <p>Use <code>separator</code> to separate subsequent item texts.</p>
     *
     * @param items         the items those texts should be joined. Elements in
     *                      <code>items</code> may be <code>null</code>.
     * @param separator     the text used to separate subsequent item texts.
     *                      [Default: ""]
     * @param emptyText     the text used when items contains no item.
     *                      [Default: ""]
     * @param textOfItem    <code>textOfItem(item)</code> returns the text of
     *                      the given <code>item</code>.
     *                      [Default: Object::toString]
     * @param nullValueText the text to use as an item's text when the item is
     *                      <code>null</code>.
     *                      [Default: "null"]
     * @return the joined texts of the items
     */
    public static <T> String textOf(
            Iterable<T> items,
            CharSequence separator,
            String emptyText,
            Function<T, String> textOfItem,
            CharSequence nullValueText) {
        StringBuilder sb = new StringBuilder();
        appendTextOf(sb, items, separator, emptyText, textOfItem, nullValueText);
        return sb.toString();
    }

    /**
     * Return the texts of the <code>items</code>, stringed together to a single
     * text.
     *
     * <p>Convenience overload of {@link #textOf(Iterable, CharSequence, String, Function, CharSequence)},
     * using default values for omitted parameter.</p>
     */
    public static <T> String textOf(
            Iterable<T> items,
            CharSequence separator,
            Function<T, String> textOfItem) {
        return textOf(items, separator, EMPTY_TEXT_DEFAULT, textOfItem, StringUtil.NULL_STRING);
    }

    /**
     * Sorts the texts of the {@code iterable}'s items (as defined by
     * {@code itemTextProvider}) and returns them as {@code '\n'}-separated lines.
     * <p>
     * {@code '\n'} characters in the items' texts are not escaped, i.e. you may
     * end up with more lines than items if items' texts contain {@code '\n'}
     * characters.
     *
     * @param iterable         provides the items to work on
     * @param itemTextProvider (default Objects::toString)
     */
    public static <T> String asSortedLines(Iterable<T> iterable,
                                           Function<T, String> itemTextProvider) {
        return addAll(new ArrayList<>(), iterable)
                .stream()
                .map(itemTextProvider)
                .sorted()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Sorts the texts of the {@code iterable}'s items (as defined by
     * {@code toString}) and returns them as {@code '\n'}-separated lines.
     * <p>
     * {@code '\n'} characters in the items' texts are not escaped, i.e. you may
     * end up with more lines than items if items' texts contain {@code '\n'}
     * characters.
     *
     * @param iterable provides the items to work on
     */
    public static <T> String asSortedLines(Iterable<T> iterable) {
        return asSortedLines(iterable, Objects::toString);
    }

    /**
     * Add all items of the <code>iterable</code> to the <code>collection</code>
     * and return the <code>collection</code>.
     */
    private static <S, T extends S> Collection<S> addAll(Collection<S> collection, Iterable<T> iterable) {
        for (T i : iterable) {
            collection.add(i);
        }
        return collection;
    }

    /**
     * Append the texts of the <code>items</code>, stringed together to a single
     * text, to the given <code>stringBuilder</code>.
     *
     * <p>Use <code>textOfItem(item)</code> as the text for an individual item.
     * When an item is <code>null</code> use the <code>nullValueText</code>.</p>
     *
     * <p>Use <code>separator</code> to separate subsequent item texts.</p>
     *
     * @param items         the items those texts should be joined. Elements in
     *                      <code>items</code> may be <code>null</code>.
     * @param separator     the text used to separate subsequent item texts.
     *                      [Default: ""]
     * @param emptyText     the text used when items contains no item.
     *                      [Default: ""]
     * @param textOfItem    <code>textOfItem(item)</code> returns the text of
     *                      the given <code>item</code>.
     *                      [Default: Object::toString]
     * @param nullValueText the text to use as an item's text when the item is
     *                      <code>null</code>.
     *                      [Default: "null"]
     * @param stopLength    when the text constructed reaches or exceeds a
     *                      length of <code>stopLength</code> no more item texts
     *                      are appended but an <code>"..."</code>
     *                      [Default: Integer.MAX_VALUE]
     */
    private static < T> void appendTextOf(
            StringBuilder stringBuilder,
            Iterable<@Nullable T> items,
            CharSequence separator,
            String emptyText,
            Function<T, String> textOfItem,
            CharSequence nullValueText,
            int stopLength) {

        boolean addSeparator = false;
        boolean isEmpty = true;
        for (@Nullable T item : items) {
            isEmpty = false;
            // Add a separator, but not before the first item
            if (addSeparator)
                stringBuilder.append(separator);
            else
                addSeparator = true;

            if (stringBuilder.length() >= stopLength) {
                stringBuilder.append("...");
                break;
            }
            stringBuilder.append(item == null ? nullValueText : textOfItem.apply(item));
        }
        if (isEmpty)
            stringBuilder.append(emptyText);
    }

    private static <T> void appendTextOf(
            StringBuilder stringBuilder,
            Iterable<T> items,
            CharSequence separator,
            String emptyText,
            Function<T, String> textOfItem,
            CharSequence nullValueText) {
        appendTextOf(stringBuilder, items, separator, emptyText, textOfItem,
                nullValueText, Integer.MAX_VALUE);
    }


}
