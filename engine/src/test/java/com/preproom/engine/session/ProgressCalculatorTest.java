package com.preproom.engine.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.preproom.engine.gap.GapReport;
import com.preproom.engine.interview.InterviewKit;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProgressCalculatorTest {

    @Test
    void bestAttemptPerQuestionCountsAndWeakestRequirementsComeFirst() {
        GapReport gap = new GapReport(List.of(
                new GapReport.Assessment(1, "required", "Python", "GAP", "", List.of()),
                new GapReport.Assessment(2, "required", "React", "MATCH", "", List.of())), 1, 0, 1);
        List<InterviewKit.KitQuestion> questions = List.of(
                new InterviewKit.KitQuestion(1, 1, "TECHNICAL", 2, "Q1", List.of("a", "b"), "JD", null),
                new InterviewKit.KitQuestion(2, 2, "TECHNICAL", 2, "Q2", List.of("a", "b"), "JD", null),
                new InterviewKit.KitQuestion(3, null, "BEHAVIORAL", 1, "Q3", List.of("a", "b"), "GENERAL", null));
        SessionDoc session = new SessionDoc("s", Instant.now(), "Role", gap, questions, List.of(
                new SessionDoc.Attempt(1, Instant.now(), "GRADED", 30, List.of()),
                new SessionDoc.Attempt(1, Instant.now(), "GRADED", 50, List.of()),
                new SessionDoc.Attempt(2, Instant.now(), "GRADED", 90, List.of()),
                new SessionDoc.Attempt(3, Instant.now(), "NOT_ENOUGH_TO_JUDGE", null, List.of())));

        Progress progress = ProgressCalculator.of(session);

        assertThat(progress.answered()).isEqualTo(2);
        assertThat(progress.total()).isEqualTo(3);
        assertThat(progress.averageScore()).isEqualTo(70);
        assertThat(progress.weakest().get(0).requirement()).isEqualTo("Python");
        assertThat(progress.weakest().get(0).bestScore()).isEqualTo(50);
    }
}