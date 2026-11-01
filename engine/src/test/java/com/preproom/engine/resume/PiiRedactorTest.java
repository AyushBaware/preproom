package com.preproom.engine.resume;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PiiRedactorTest {

    @Test
    void emailIsRedacted() {
        assertThat(PiiRedactor.redact("Mail me at a.b@example.com today"))
                .contains("[email]").doesNotContain("example.com");
    }

    @Test
    void phoneNumbersAreRedacted() {
        assertThat(PiiRedactor.redact("Call +91 98765 43210")).contains("[phone]").doesNotContain("98765");
        assertThat(PiiRedactor.redact("Call 9876543210")).contains("[phone]").doesNotContain("9876543210");
    }

    @Test
    void ordinaryNumbersSurvive() {
        String text = "B.Tech 2021-2025, CGPA 8.2, 3 projects";
        assertThat(PiiRedactor.redact(text)).isEqualTo(text);
    }
}