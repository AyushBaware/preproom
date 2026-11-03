package com.preproom.engine.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class GradeVerifierTest {

    private static final List<String> POINTS =
            List.of("Uses the with statement", "Guarantees cleanup on errors", "Used for files and locks");
    private static final String ANSWER =
            "I use the with statement so the file is always closed, even if an error happens.";

    @Test
    void verifiedCreditCountsAndUnverifiedCreditIsDropped() {
        GradeAnswer raw = new GradeAnswer(List.of(
                new GradeAnswer.PointJudgement(1, "covered", "I use the with statement", "Names the with statement."),
                new GradeAnswer.PointJudgement(2, "PARTIAL", "always closed, even if an error happens", "Say exceptions explicitly."),
                new GradeAnswer.PointJudgement(3, "COVERED", "locks and sockets are released", "Claimed but not written.")));

        GradeResult result = GradeVerifier.build(POINTS, raw, ANSWER);

        assertThat(result.points()).extracting(GradeResult.PointResult::coverage)
                .containsExactly("COVERED", "PARTIAL", "MISSING");
        assertThat(result.score()).isEqualTo(50);
        assertThat(result.notes()).hasSize(1);
    }

    @Test
    void pointsTheAiSkippedCountAsMissing() {
        GradeResult result = GradeVerifier.build(POINTS, new GradeAnswer(List.of()), ANSWER);
        assertThat(result.score()).isZero();
        assertThat(result.points()).extracting(GradeResult.PointResult::coverage)
                .containsOnly("MISSING");
    }

    @Test
    void wordCountIgnoresExtraWhitespace() {
        assertThat(GradeService.wordCount("  one two   three ")).isEqualTo(3);
        assertThat(GradeService.wordCount("   ")).isZero();
    }
}