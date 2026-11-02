package com.preproom.engine.interview;

import java.util.List;

/** The shape we ask the AI to return. */
public record QuestionSet(List<RawQuestion> questions) {
    public record RawQuestion(Integer requirementId, String type, Integer difficulty,
                              String text, List<String> goodAnswerPoints, String jdQuote) {}
}