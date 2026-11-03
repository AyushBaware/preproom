package com.preproom.engine.session;

import static org.assertj.core.api.Assertions.assertThat;

import com.preproom.engine.gap.GapReport;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionServiceTest {

    @Test
    void evidenceQuotesAreNotStored() {
        GapReport gap = new GapReport(List.of(
                new GapReport.Assessment(1, "required", "React", "MATCH", "listed", List.of("Frontend React"))),
                1, 0, 0);
        GapReport stripped = SessionService.stripEvidence(gap);
        assertThat(stripped.assessments().get(0).evidence()).isEmpty();
        assertThat(stripped.assessments().get(0).verdict()).isEqualTo("MATCH");
    }
}