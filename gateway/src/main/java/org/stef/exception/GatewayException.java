package org.stef.exception;

import lombok.Getter;

@Getter
public abstract class GatewayException extends RuntimeException {

    private final int statusCode;

    protected GatewayException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}