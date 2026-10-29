package com.preproom.engine.jd;

/** One extracted fact plus the exact words from the source that support it. */
public record Item(String text, String quote) {}