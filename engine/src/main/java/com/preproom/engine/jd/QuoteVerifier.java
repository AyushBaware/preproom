package com.preproom.engine.jd;

import java.util.Locale;

/** Checks that a quote really appears in the source text. */
public final class QuoteVerifier {

    private QuoteVerifier() {}

    public static boolean appearsIn(String quote, String sourceText) {
        if (quote == null || quote.isBlank() || sourceText == null) {
            return false;
        }
        return normalize(sourceText).contains(normalize(quote));
    }

    static String normalize(String s) {
        return s.toLowerCase(Locale.ROOT)
                .replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u201c', '"').replace('\u201d', '"')
                .replace('\u2013', '-').replace('\u2014', '-')
                .replace('\u2022', ' ')
                .replaceAll("[\\s\\u00a0]+", " ")
                .trim();
    }
}