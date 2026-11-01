package com.preproom.engine.resume;

/** One fact from a resume plus the exact words that support it. */
public record ResumeFact(String category, String text, String quote) {}