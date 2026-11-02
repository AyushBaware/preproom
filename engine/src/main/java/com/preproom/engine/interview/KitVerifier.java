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

    private KitVerifier() {}

    public static InterviewKit build(QuestionSet raw, Set<Integer> requirementIds, String jdText) {
        List<InterviewKit.KitQuestion> kept = new ArrayList<>();
        List<String> rejected = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (raw != null && raw.questions() != null) {
            for (QuestionSet.RawQuestion q : raw.questions()) {
                if (q == null || q.text() == null || q.text().isBlank()) {
                    rejected.add("empty question");
                    continue;
                }
                String text = q.text().trim();
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
                        : q.goodAnswerPoints().stream().filter(p -> p != null && !p.isBlank()).map(String::trim).toList();
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
                kept.add(new InterviewKit.KitQuestion(kept.size() + 1, requirementId, type, difficulty, text,
                        points, fromJd ? "JD" : "GENERAL", fromJd ? q.jdQuote() : null));
            }
        }
        return new InterviewKit(kept, rejected);
    }
}