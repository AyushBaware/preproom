package com.preproom.engine.resume;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ResumeVerifierTest {

    private static final String SOURCE = "Built REST APIs using Spring Boot and MongoDB. B.Tech in Computer Science.";

    @Test
    void keepsVerifiedFactsAndRejectsInventedOnes() {
        Resume raw = new Resume(List.of(
                new ResumeFact("skill", "Spring Boot", "Built REST APIs using Spring Boot"),
                new ResumeFact("SKILL", "Kubernetes", "Deployed on Kubernetes"),
                new ResumeFact("HOBBY", "Chess", "Chess")));

        ResumeExtractionResult result = ResumeVerifier.keepVerified(raw, SOURCE);

        assertThat(result.resume().facts()).extracting(ResumeFact::text).containsExactly("Spring Boot");
        assertThat(result.resume().facts().get(0).category()).isEqualTo("SKILL");
        assertThat(result.rejected()).hasSize(2);
    }
}