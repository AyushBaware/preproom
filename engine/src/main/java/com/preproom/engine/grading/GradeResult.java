package com.preproom.engine.grading;

import java.util.List;

public record GradeResult(String status, Integer score, List<PointResult> points, List<String> notes) {
    public record PointResult(int index, String point, String coverage, String quote, String feedback) {}
}