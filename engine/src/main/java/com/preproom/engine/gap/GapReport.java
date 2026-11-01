package com.preproom.engine.gap;

import java.util.List;

public record GapReport(List<Assessment> assessments, int matches, int partials, int gaps) {
    public record Assessment(int id, String level, String requirement, String verdict,
                             String reason, List<String> evidence) {}
}