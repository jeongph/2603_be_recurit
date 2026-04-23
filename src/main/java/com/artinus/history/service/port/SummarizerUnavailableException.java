package com.artinus.history.service.port;

public class SummarizerUnavailableException extends RuntimeException {
    public SummarizerUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
