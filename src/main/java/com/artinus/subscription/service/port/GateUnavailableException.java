package com.artinus.subscription.service.port;

public class GateUnavailableException extends RuntimeException {
    public GateUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public GateUnavailableException(String message) {
        super(message);
    }
}
