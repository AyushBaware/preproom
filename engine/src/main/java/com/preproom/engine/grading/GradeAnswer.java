package com.preproom.engine.grading;

import java.util.List;

/** The shape we ask the AI to return. */
public record GradeAnswer(List<PointJudgement> points) {
    public record PointJudgement(int index, String coverage, String quote, String feedback) {}
}