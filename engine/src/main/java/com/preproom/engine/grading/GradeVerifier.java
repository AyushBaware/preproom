package com.preproom.engine.grading;

import com.preproom.engine.jd.QuoteVerifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Turns the AI's judgement into a score in which every credit is backed by a verified quote. */
public final class GradeVerifier {

    private GradeVerifier() {}

    public static GradeResult build(List<String> points, GradeAnswer raw, String answer) {
        Map<Integer, GradeAnswer.PointJudgement> byIndex = new HashMap<>();
        if (raw != null && raw.points() != null) {
            for (GradeAnswer.PointJudgement j : raw.points()) {
                if (j != null) {
                    byIndex.putIfAbsent(j.index(), j);
                }
            }
        }
        List<GradeResult.PointResult> results = new ArrayList<>();
        int covered = 0;
        int partial = 0;
        int dropped = 0;
        for (int i = 0; i < points.size(); i++) {
            int index = i + 1;
            GradeAnswer.PointJudgement j = byIndex.get(index);
            String coverage = "MISSING";
            String quote = null;
            String feedback = "Not assessed by the AI, so treated as missing";
            if (j != null) {
                String claimed = j.coverage() == null ? "" : j.coverage().trim().toUpperCase(Locale.ROOT);
                String text = j.feedback() == null ? "" : j.feedback().trim();
                if (claimed.equals("MISSING")) {
                    feedback = text;
                } else if (claimed.equals("COVERED") || claimed.equals("PARTIAL")) {
                    if (QuoteVerifier.appearsIn(j.quote(), answer)) {
                        coverage = claimed;
                        quote = j.quote();
                        feedback = text;
                        if (claimed.equals("COVERED")) {
                            covered++;
                        } else {
                            partial++;
                        }
                    } else {
                        dropped++;
                        feedback = "The AI claimed credit, but no supporting words were found in your answer";
                    }
                } else {
                    feedback = "Invalid coverage value from the AI, treated as missing";
                }
            }
            results.add(new GradeResult.PointResult(index, points.get(i), coverage, quote, feedback));
        }
        int score = (int) Math.round(100.0 * (covered + 0.5 * partial) / points.size());
        List<String> notes = new ArrayList<>();
        if (dropped > 0) {
            notes.add(dropped + " credit claim(s) were dropped because their quotes were not in your answer");
        }
        return new GradeResult("GRADED", score, results, notes);
    }
}