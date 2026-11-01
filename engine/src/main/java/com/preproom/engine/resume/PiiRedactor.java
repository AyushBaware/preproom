package com.preproom.engine.resume;

import java.util.regex.Pattern;

/** Best-effort removal of emails and phone numbers before text goes to an AI provider. */
public final class PiiRedactor {

    private static final Pattern EMAIL =
            Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE = Pattern.compile(
            "(?<!\\d)(?:\\+?91[\\s-]?)?[6-9]\\d{4}[\\s-]?\\d{5}(?!\\d)|\\+\\d{1,3}[\\s-]?\\d[\\d\\s-]{7,13}\\d");

    private PiiRedactor() {}

    public static String redact(String text) {
        if (text == null) {
            return "";
        }
        String result = EMAIL.matcher(text).replaceAll("[email]");
        return PHONE.matcher(result).replaceAll("[phone]");
    }
}