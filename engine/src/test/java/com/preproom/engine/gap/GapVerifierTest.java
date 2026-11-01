package com.preproom.engine.gap;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class GapVerifierTest {

    private static final String RESUME =
            "Built REST APIs using Spring Boot. Used React for the frontend. B.Tech Computer Science.";

    @Test
    void verifiedMatchesStayAndUnverifiedClaimsBecomeGaps() {
        List<GapVerifier.Requirement> requirements = List.of(
                new GapVerifier.Requirement(1, "required", "React"),
                new GapVerifier.Requirement(2, "required", "Kubernetes"),
                new GapVerifier.Requirement(3, "preferred", "Docker"),
                new GapVerifier.Requirement(4, "required", "Spring Boot"));
        ModelAnswer answer = new ModelAnswer(List.of(
                new ModelAnswer.ModelItem(1, "match", "React is used", List.of("Used React for the frontend")),
                new ModelAnswer.ModelItem(2, "MATCH", "claimed", List.of("Deployed on Kubernetes")),
                new ModelAnswer.ModelItem(3, "GAP", "not mentioned", List.of())));

        GapReport report = GapVerifier.build(requirements, answer, RESUME);

        assertThat(report.assessments()).extracting(GapReport.Assessment::verdict)
                .containsExactly("MATCH", "GAP", "GAP", "GAP");
        assertThat(report.matches()).isEqualTo(1);
        assertThat(report.gaps()).isEqualTo(3);
        assertThat(report.assessments().get(3).reason()).contains("Not assessed");
    }
}