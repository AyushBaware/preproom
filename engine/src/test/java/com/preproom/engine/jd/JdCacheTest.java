package com.preproom.engine.jd;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JdCacheTest {

    @Test
    void sameDocumentWithDifferentWhitespaceGetsTheSameKey() {
        assertThat(JdCache.keyFor("Backend: FastAPI / Python"))
                .isEqualTo(JdCache.keyFor("backend:  fastapi /\n python"));
    }

    @Test
    void differentDocumentsGetDifferentKeys() {
        assertThat(JdCache.keyFor("Backend: FastAPI")).isNotEqualTo(JdCache.keyFor("Backend: Spring Boot"));
    }

    @Test
    void dotsAndDollarsInMapKeysAreReplaced() {
        JobDescription jd = new JobDescription(null, null, null, null, null,
                List.of(), List.of(), List.of(), List.of(), null, null,
                Map.of("pay.grade", "L2", "$x", "y"));
        assertThat(JdCache.withSafeKeys(jd).otherDetails()).containsOnlyKeys("pay_grade", "_x");
    }
}