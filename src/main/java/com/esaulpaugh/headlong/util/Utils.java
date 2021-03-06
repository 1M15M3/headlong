package com.esaulpaugh.headlong.util;

import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static final Charset CHARSET_ASCII = Charset.forName("US-ASCII");

    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    public static String validateChars(Pattern pattern, String name) throws ParseException {
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            final char c = name.charAt(matcher.start());
            throw new ParseException(
                    "illegal char " + escapeChar(c) + " \'" + c + "\' @ index " + matcher.start(),
                    matcher.start()
            );
        }
        return name;
    }

    public static String escapeChar(char c) {
        String hex = Integer.toHexString((int) c);
        switch (hex.length()) {
        case 1: return "\\u000" + hex;
        case 2: return "\\u00" + hex;
        case 3: return "\\u0" + hex;
        case 4: return "\\u" + hex;
        default: return "\\u0000";
        }
    }
}
