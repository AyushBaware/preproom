package com.preproom.engine.resume;

import com.preproom.engine.jd.QuoteVerifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Keeps only facts whose quote really appears in the resume text. */
public final class ResumeVerifier {

    private static final Set<String> CATEGORIES =
            Set.of("SKILL", "PROJECT", "EXPERIENCE", "EDUCATION", "CERTIFICATION", "ACHIEVEMENT");

    private ResumeVerifier() {}

    public static ResumeExtractionResult keepVerified(Resume resume, String source) {
        List<ResumeFact> kept = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        if (resume != null && resume.facts() != null) {
            for (ResumeFact fact : resume.facts()) {
                if (fact == null) {
                    rejected.add("(empty fact)");
                    continue;
                }
                String category = fact.category() == null ? "" : fact.category().trim().toUpperCase(Locale.ROOT);
                if (!CATEGORIES.contains(category)) {
                    rejected.add("unknown category: " + fact.category() + " | " + fact.text());
                } else if (!QuoteVerifier.appearsIn(fact.quote(), source)) {
                    rejected.add(category + ": " + fact.text() + " | quote: " + fact.quote());
                } else {
                    kept.add(new ResumeFact(category, fact.text(), fact.quote()));
                }
            }
        }
        return new ResumeExtractionResult(new Resume(kept), rejected);
    }
}