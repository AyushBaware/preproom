package com.preproom.engine.gap;

import com.preproom.engine.jd.QuoteVerifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Turns the AI's answer into a report in which every claim is backed by a verified quote. */
public final class GapVerifier {

    public record Requirement(int id, String level, String text) {}

    private GapVerifier() {}

    public static GapReport build(List<Requirement> requirements, ModelAnswer answer, String resumeText) {
        Map<Integer, ModelAnswer.ModelItem> byId = new HashMap<>();
        if (answer != null && answer.assessments() != null) {
            for (ModelAnswer.ModelItem item : answer.assessments()) {
                if (item != null) {
                    byId.putIfAbsent(item.id(), item);
                }
            }
        }
        List<GapReport.Assessment> out = new ArrayList<>();
        int matches = 0;
        int partials = 0;
        int gaps = 0;
        for (Requirement r : requirements) {
            String verdict = "GAP";
            String reason = "Not assessed by the AI, so treated as a gap";
            List<String> evidence = new ArrayList<>();
            ModelAnswer.ModelItem item = byId.get(r.id());
            if (item != null) {
                String claimed = item.verdict() == null ? "" : item.verdict().trim().toUpperCase(Locale.ROOT);
                List<String> verified = verifiedQuotes(item.evidence(), resumeText);
                if (claimed.equals("GAP")) {
                    reason = item.reason() == null ? "" : item.reason();
                } else if (claimed.equals("MATCH") || claimed.equals("PARTIAL")) {
                    if (verified.isEmpty()) {
                        reason = "The AI claimed " + claimed + ", but no quote could be verified in the resume";
                    } else {
                        verdict = claimed;
                        reason = item.reason() == null ? "" : item.reason();
                        evidence = verified;
                    }
                } else {
                    reason = "Invalid verdict from the AI, treated as a gap";
                }
            }
            switch (verdict) {
                case "MATCH" -> matches++;
                case "PARTIAL" -> partials++;
                default -> gaps++;
            }
            out.add(new GapReport.Assessment(r.id(), r.level(), r.text(), verdict, reason, evidence));
        }
        return new GapReport(out, matches, partials, gaps);
    }

    private static List<String> verifiedQuotes(List<String> quotes, String resumeText) {
        List<String> verified = new ArrayList<>();
        if (quotes != null) {
            for (String q : quotes) {
                if (QuoteVerifier.appearsIn(q, resumeText)) {
                    verified.add(q);
                }
            }
        }
        return verified;
    }
}