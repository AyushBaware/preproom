package com.preproom.engine.gap;

import java.util.List;

/** The shape we ask the AI to return. */
public record ModelAnswer(List<ModelItem> assessments) {
    public record ModelItem(int id, String verdict, String reason, List<String> evidence) {}
}