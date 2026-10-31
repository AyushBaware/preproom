package com.preproom.engine.jd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QuoteVerifierTest {

    private static final String SOURCE = """
            Tech Stack
            Frontend: React
            Backend: FastAPI / Python
            Infrastructure: Linux / Cloud
            Plus: APIs, Git/GitHub, databases and AI tools
            """;

    @Test
    void exactQuoteIsFound() {
        assertThat(QuoteVerifier.appearsIn("Backend: FastAPI / Python", SOURCE)).isTrue();
    }

    @Test
    void caseAndWhitespaceDifferencesAreIgnored() {
        assertThat(QuoteVerifier.appearsIn("backend:   fastapi /\n python", SOURCE)).isTrue();
    }

    @Test
    void inventedQuoteIsRejected() {
        assertThat(QuoteVerifier.appearsIn("Backend: Spring Boot", SOURCE)).isFalse();
    }

    @Test
    void blankOrNullQuoteIsRejected() {
        assertThat(QuoteVerifier.appearsIn("", SOURCE)).isFalse();
        assertThat(QuoteVerifier.appearsIn(null, SOURCE)).isFalse();
    }

    @Test
    void lineBreakHyphenationIsIgnored() {
        assertThat(QuoteVerifier.appearsIn("improve decision-making.", "improve decision-\nmaking.")).isTrue();
    }

    @Test
    void veryShortQuotesAreRejected() {
        assertThat(QuoteVerifier.appearsIn("R", "SQL, Python and/or R")).isFalse();
    }
}