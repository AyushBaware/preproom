package com.preproom.engine.jd;

/** Checks that a quote really appears in the source text, ignoring case, spacing and punctuation. */
public final class QuoteVerifier {

    private static final int MIN_LETTERS_AND_DIGITS = 3;

    private QuoteVerifier() {}

    public static boolean appearsIn(String quote, String sourceText) {
        if (quote == null || sourceText == null) {
            return false;
        }
        String q = fingerprint(quote);
        if (q.length() < MIN_LETTERS_AND_DIGITS) {
            return false;
        }
        return fingerprint(sourceText).contains(q);
    }

    /** Keeps only letters and digits, lower-cased, so line-break hyphens, bullets and curly quotes no longer matter. */
    public static String fingerprint(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        s.codePoints().forEach(cp -> {
            if (Character.isLetterOrDigit(cp)) {
                sb.appendCodePoint(Character.toLowerCase(cp));
            }
        });
        return sb.toString();
    }
}