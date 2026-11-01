package com.preproom.engine.document;

/** The AI step failed or returned an answer we could not read. */
public class AiExtractionException extends RuntimeException {
    public AiExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}