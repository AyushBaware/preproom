package com.preproom.engine.session;

import java.util.List;

public record Progress(int answered, int total, Integer averageScore, List<WeakSpot> weakest) {
    public record WeakSpot(String requirement, int bestScore) {}
}