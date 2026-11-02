package com.preproom.engine.interview;

import java.util.List;

public record InterviewKit(List<KitQuestion> questions, List<String> rejected) {
    public record KitQuestion(int id, Integer requirementId, String type, int difficulty, String text,
                              List<String> goodAnswerPoints, String basis, String jdQuote) {}
}