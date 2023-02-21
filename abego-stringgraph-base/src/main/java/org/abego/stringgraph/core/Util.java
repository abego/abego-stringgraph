package org.abego.stringgraph.core;

import org.abego.commons.lang.StringUtil;
import org.abego.commons.lang.exception.MustNotInstantiateException;

import java.util.regex.Pattern;

final class Util {
    Util() {
        throw new MustNotInstantiateException();
    }

    static String quotedIfNeeded(String s) {
        return needsQuotes(s) ? StringUtil.quoted2(s) : s;
    }

    private final static Pattern NO_QUOTES_NEEDED_PATTERN = Pattern.compile(
            "[-\\w_+*=.:;/@?&#()\\[\\]{}<>]+");

    private static boolean needsQuotes(String s) {
        return !NO_QUOTES_NEEDED_PATTERN.matcher(s).matches();
    }

}
