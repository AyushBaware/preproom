package com.preproom.engine.interview;

import com.preproom.engine.jd.QuoteVerifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Keeps only well-formed questions and labels each one as JD-based or general. */
public final class KitVerifier {

    private static final Set<String> TYPES = Set.of("TECHNICAL", "BEHAVIORAL", "SCENARIO");
    private static final int MAX_QUESTION_CHARS = 300;

    private static final List<String> STAR_POINTS = List.of(
            "Describes a specific situation or task",
            "Explains what the candidate personally did",
            "States the result or outcome",
            "Reflects on what was learned or would be done differently");

    private static final java.util.Map<String, Integer> LIMITS =
            java.util.Map.of("TECHNICAL", 5, "BEHAVIORAL", 2, "SCENARIO", 1);

    static String normalizeText(String s) {
        return s.replace('\u2010', '-').replace('\u2011', '-').replace('\u2012', '-')
                .replace('\u2013', '-').replace('\u2014', '-')
                .replace('\u2018', '\'').replace('\u2019', '\'')
                .replace('\u201c', '"').replace('\u201d', '"')
                .replaceAll("\\s+", " ").trim();
    }

    private KitVerifier() {}

    public static InterviewKit build(QuestionSet raw, Set<Integer> requirementIds, String jdText) {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        List<InterviewKit.KitQuestion> kept = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (raw != null && raw.questions() != null) {
            for (QuestionSet.RawQuestion q : raw.questions()) {
                if (q == null || q.text() == null || q.text().isBlank()) {
                    rejected.add("empty question");
                    continue;
                }
                String text = normalizeText(q.text());
                String type = q.type() == null ? "" : q.type().trim().toUpperCase(Locale.ROOT);
                if (!TYPES.contains(type)) {
                    rejected.add("unknown type " + q.type() + ": " + text);
                    continue;
                }
                if (text.length() > MAX_QUESTION_CHARS) {
                    rejected.add("too long: " + text.substring(0, 60) + "...");
                    continue;
                }
                List<String> points = q.goodAnswerPoints() == null ? List.of()
                        : q.goodAnswerPoints().stream().filter(p -> p != null && !p.isBlank())
                                .map(KitVerifier::normalizeText)
                                .filter(p -> p.length() <= 200)
                                .limit(5)
                                .toList();
                if (type.equals("BEHAVIORAL")) {
                    points = STAR_POINTS;
                }
                if (points.size() < 2) {
                    rejected.add("fewer than 2 answer points: " + text);
                    continue;
                }
                if (!seen.add(QuoteVerifier.fingerprint(text))) {
                    rejected.add("duplicate: " + text);
                    continue;
                }
                Integer requirementId = q.requirementId() != null && requirementIds.contains(q.requirementId())
                        ? q.requirementId() : null;
                int difficulty = q.difficulty() == null ? 2 : Math.max(1, Math.min(3, q.difficulty()));
                boolean fromJd = QuoteVerifier.appearsIn(q.jdQuote(), jdText);
                if (counts.merge(type, 1, Integer::sum) > LIMITS.get(type)) {
                    rejected.add("over the limit for " + type + ": " + text);
                    continue;
                }
                kept.add(new InterviewKit.KitQuestion(kept.size() + 1, requirementId, type, difficulty, text,
                        points, fromJd ? "JD" : "GENERAL", fromJd ? q.jdQuote() : null));
            }
        }
        return new InterviewKit(kept, rejected);
    }
}