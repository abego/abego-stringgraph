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

import java.util.regex.Pattern;

// Original: https://github.com/abego/commons
public final class StringUtil {
    public static final String NULL_STRING = "null"; //NON-NLS
    
    StringUtil() {
        throw new UnsupportedOperationException();
    }

    //region quoted2 (from abego-commons)

    private static final char DOUBLE_QUOTE_CHAR = '"'; // NON-NLS
    private static final char BEL_CHAR = '\b'; // NON-NLS
    private static final char FORM_FEED_CHAR = '\f'; // NON-NLS
    private static final char NEWLINE_CHAR = '\n'; // NON-NLS
    private static final char CARRIAGE_RETURN_CHAR = '\r'; // NON-NLS
    private static final char TAB_CHAR = '\t'; // NON-NLS
    private static final char BACKSLASH_CHAR = '\\'; // NON-NLS
    private static final char SINGLE_QUOTE_CHAR = '\''; // NON-NLS
    private static final int FIRST_PRINTABLE_ASCII_CHAR_VALUE = 32;
    private static final int LAST_PRINTABLE_ASCII_CHAR_VALUE = 127;

    public static String quoted2(@Nullable String text) {
        return quoted2(text, NULL_STRING);
    }

    /**
     * Return a (double) quoted version of <code>text</code>, with non-ASCII
     * characters not escaped.
     *
     * @param text       the string to quote
     * @param nullResult the String to be returned for null values.
     */
    private static String quoted2(@Nullable String text, String nullResult) {
        return quotedHelper(text, nullResult, DOUBLE_QUOTE_CHAR, "'", "\\\"", StringUtil::append);
    }

    private interface StringBuilderAppender {
        void append(StringBuilder builder, char c);
    }

    private static String quotedHelper(
            @Nullable String text,
            String nullResult,
            char quoteChar,
            String singleQuoteText,
            String doubleQuoteText,
            StringBuilderAppender nonAsciiCharHandler) {

        if (text == null) {
            return nullResult;
        }
        StringBuilder result = new StringBuilder();
        result.append(quoteChar);
        appendEscapedString(result, text, singleQuoteText, doubleQuoteText, nonAsciiCharHandler);
        result.append(quoteChar);
        return result.toString();
    }
    /**
     * Append the <code>text</code> to the stringBuilder, but in an escaped form,
     * as if the string would be written inside a String literal.
     *
     * @param stringBuilder        the StringBuilder to append to
     * @param text                 the String to be escaped and appended
     * @param singleQuoteText      the text to use to "escape" a single quote char
     * @param nonASCIICharAppender used to append the text for non-ASCII chars
     */
    private static void appendEscapedString(
            StringBuilder stringBuilder,
            @Nullable String text,
            String singleQuoteText,
            String doubleQuoteText,
            StringBuilderAppender nonASCIICharAppender) {
        if (text != null) {
            int length = text.length();
            for (int i = 0; i < length; i++) {
                char c = text.charAt(i);
                appendEscapedChar(stringBuilder, c, singleQuoteText, doubleQuoteText, nonASCIICharAppender);
            }
        }
    }

    private static void appendEscapedChar(
            StringBuilder stringBuilder,
            char character,
            String singleQuoteText,
            String doubleQuoteText,
            StringBuilderAppender nonASCIICharAppender) {

        switch (character) {
            case BEL_CHAR:
                stringBuilder.append("\\b"); //NON-NLS
                break;

            case FORM_FEED_CHAR:
                stringBuilder.append("\\f"); //NON-NLS
                break;

            case NEWLINE_CHAR:
                stringBuilder.append("\\n"); //NON-NLS
                break;

            case CARRIAGE_RETURN_CHAR:
                stringBuilder.append("\\r"); //NON-NLS
                break;

            case TAB_CHAR:
                stringBuilder.append("\\t"); //NON-NLS
                break;

            case BACKSLASH_CHAR:
                stringBuilder.append("\\\\");
                break;

            case DOUBLE_QUOTE_CHAR:
                stringBuilder.append(doubleQuoteText);
                break;

            case SINGLE_QUOTE_CHAR:
                stringBuilder.append(singleQuoteText);
                break;

            default:
                if (character < FIRST_PRINTABLE_ASCII_CHAR_VALUE) {
                    appendUnicodeEscaped(stringBuilder, character);
                } else if (character > LAST_PRINTABLE_ASCII_CHAR_VALUE) {
                    nonASCIICharAppender.append(stringBuilder, character);
                } else {
                    stringBuilder.append(character);
                }
                break;
        }
    }

    private static void appendUnicodeEscaped(StringBuilder stringBuilder, char character) {
        String n = Integer.toHexString(character);
        stringBuilder.append("\\u"); //NON-NLS
        stringBuilder.append("0000".substring(n.length()));
        stringBuilder.append(n);
    }

    private static void append(StringBuilder stringBuilder, char character) {
        stringBuilder.append(character); //NON-NLS
    }

    //endregion

    //region quotedIfNeeded
    
    public static String quotedIfNeeded(String s) {
        return needsQuotes(s) ? StringUtil.quoted2(s) : s;
    }

    private final static Pattern NO_QUOTES_NEEDED_PATTERN = Pattern.compile(
            "[-\\w_+*=.:;/@?&#()\\[\\]{}<>]+");

    private static boolean needsQuotes(String s) {
        return !NO_QUOTES_NEEDED_PATTERN.matcher(s).matches();
    }


    //endregion
}
