package com.preproom.engine.jd;

import java.util.ArrayList;
import java.util.List;

/** Drops anything the model claimed that cannot be found in the source text, and records what was dropped. */
public final class JdVerifier {

    private JdVerifier() {}

    public static ExtractionResult keepVerified(JobDescription jd, String source) {
        List<String> rejected = new ArrayList<>();
        Logistics logistics = jd.logistics() == null ? null : new Logistics(
                scalar("duration", jd.logistics().duration(), source, rejected),
                scalar("stipend", jd.logistics().stipend(), source, rejected),
                scalar("ppo", jd.logistics().ppo(), source, rejected),
                scalar("ctc", jd.logistics().ctc(), source, rejected),
                scalar("deadline", jd.logistics().deadline(), source, rejected));
        JobDescription cleaned = new JobDescription(
                scalar("company", jd.company(), source, rejected),
                scalar("roleTitle", jd.roleTitle(), source, rejected),
                scalar("employmentType", jd.employmentType(), source, rejected),
                scalar("location", jd.location(), source, rejected),
                scalar("workMode", jd.workMode(), source, rejected),
                filter("requiredSkills", jd.requiredSkills(), source, rejected),
                filter("preferredSkills", jd.preferredSkills(), source, rejected),
                filter("responsibilities", jd.responsibilities(), source, rejected),
                filter("softTraits", jd.softTraits(), source, rejected),
                jd.applicationProcess(),
                logistics,
                jd.otherDetails());
        return new ExtractionResult(cleaned, rejected);
    }

        private static String scalar(String field, String value, String source, List<String> rejected) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (looksLetterSpaced(value)) {
            rejected.add(field + ": letter-spaced header text: " + value);
            return null;
        }
        if (QuoteVerifier.appearsIn(value, source)) {
            return value;
        }
        rejected.add(field + ": " + value);
        return null;
    }

    /** True for header text like "P R E D I X I O N", which PDFs produce from letter-spaced fonts. */
    static boolean looksLetterSpaced(String value) {
        String[] tokens = value.trim().split("\\s+");
        if (tokens.length < 5) {
            return false;
        }
        long singles = java.util.Arrays.stream(tokens).filter(t -> t.length() == 1).count();
        return singles * 10 >= tokens.length * 6L;
    }

    private static List<Item> filter(String field, List<Item> items, String source, List<String> rejected) {
        List<Item> kept = new ArrayList<>();
        if (items == null) {
            return kept;
        }
        for (Item item : items) {
            if (item == null) {
                rejected.add(field + ": (empty item)");
            } else if (QuoteVerifier.appearsIn(item.quote(), source)) {
                kept.add(item);
            } else {
                rejected.add(field + ": " + item.text() + " | quote: " + item.quote());
            }
        }
        return kept;
    }
}