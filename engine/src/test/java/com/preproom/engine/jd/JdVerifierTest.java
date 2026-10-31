package com.preproom.engine.jd;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class JdVerifierTest {

    private static final String SOURCE =
            "Platform Engineering Intern at Predixion AI. Frontend: React. Backend: FastAPI / Python.";

    @Test
    void keepsVerifiedItemsAndDropsInventedOnes() {
        JobDescription raw = new JobDescription("Predixion AI", "Platform Engineering Intern", null, "Pune", null,
                List.of(new Item("React", "Frontend: React"), new Item("Spring Boot", "Backend: Spring Boot")),
                List.of(), List.of(), List.of(), null, null, null);

        ExtractionResult result = JdVerifier.keepVerified(raw, SOURCE);

        assertThat(result.jd().company()).isEqualTo("Predixion AI");
        assertThat(result.jd().location()).isNull();
        assertThat(result.jd().requiredSkills()).extracting(Item::text).containsExactly("React");
        assertThat(result.rejected()).hasSize(2);
    }

    @Test
    void letterSpacedHeaderTextIsRejected() {
        assertThat(JdVerifier.looksLetterSpaced("P R E D I X I O N  A I  L T D .")).isTrue();
        assertThat(JdVerifier.looksLetterSpaced("Predixion AI")).isFalse();
    }
}