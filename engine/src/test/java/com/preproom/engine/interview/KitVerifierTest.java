package com.preproom.engine.interview;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class KitVerifierTest {

    private static final String JD = "Backend: FastAPI / Python. Working with Linux-based environments and servers.";

    @Test
    void labelsAndFiltersQuestions() {
        QuestionSet raw = new QuestionSet(List.of(
                new QuestionSet.RawQuestion(2, "technical", 2, "What is a FastAPI dependency?",
                        List.of("Reusable injected logic", "Declared via Depends"), "Backend: FastAPI / Python"),
                new QuestionSet.RawQuestion(99, "TECHNICAL", 9, "Explain Linux file permissions.",
                        List.of("rwx bits", "chmod"), "They always ask about Linux"),
                new QuestionSet.RawQuestion(1, "TRIVIA", 1, "What is 2+2?", List.of("4", "four"), null),
                new QuestionSet.RawQuestion(1, "BEHAVIORAL", 1, "Explain Linux file permissions.",
                        List.of("a", "b"), null),
                new QuestionSet.RawQuestion(null, "SCENARIO", 2, "A server is slow. What do you check?",
                        List.of("Only one point"), null)));

        InterviewKit kit = KitVerifier.build(raw, Set.of(1, 2), JD);

        assertThat(kit.questions()).hasSize(2);
        assertThat(kit.questions().get(0).basis()).isEqualTo("JD");
        assertThat(kit.questions().get(0).requirementId()).isEqualTo(2);
        assertThat(kit.questions().get(1).basis()).isEqualTo("GENERAL");
        assertThat(kit.questions().get(1).requirementId()).isNull();
        assertThat(kit.questions().get(1).difficulty()).isEqualTo(3);
        assertThat(kit.rejected()).hasSize(3);
    }

    @Test
    void typeLimitsAreEnforced() {
        List<QuestionSet.RawQuestion> many = new java.util.ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            many.add(new QuestionSet.RawQuestion(1, "TECHNICAL", 2, "Technical question number " + i + "?",
                    List.of("point a", "point b"), null));
        }
        InterviewKit kit = KitVerifier.build(new QuestionSet(many), Set.of(1), JD);
        assertThat(kit.questions()).hasSize(5);
        assertThat(kit.rejected()).hasSize(2);
    }

    @Test
    void specialHyphensAreNormalised() {
        QuestionSet raw = new QuestionSet(List.of(new QuestionSet.RawQuestion(1, "TECHNICAL", 2,
                "Explain server\u2011side rendering.", List.of("point a", "point b"), null)));
        assertThat(KitVerifier.build(raw, Set.of(1), JD).questions().get(0).text())
                .isEqualTo("Explain server-side rendering.");
    }

    @Test
    void answerPointsAreCappedAtFive() {
        QuestionSet raw = new QuestionSet(List.of(new QuestionSet.RawQuestion(1, "TECHNICAL", 2,
                "A question about caching?", List.of("p1", "p2", "p3", "p4", "p5", "p6", "p7"), null)));
        assertThat(KitVerifier.build(raw, Set.of(1), JD).questions().get(0).goodAnswerPoints()).hasSize(5);
    }

    @Test
    void behavioralQuestionsUseTheStarRubric() {
        QuestionSet raw = new QuestionSet(List.of(new QuestionSet.RawQuestion(null, "BEHAVIORAL", 1,
                "Tell me about a conflict you resolved.", List.of("x", "y"), null)));
        List<String> points = KitVerifier.build(raw, Set.of(1), JD).questions().get(0).goodAnswerPoints();
        assertThat(points).hasSize(4);
        assertThat(points.get(0)).contains("situation");
    }
}