package org.stef.exception;

import lombok.Getter;

@Getter
public abstract class OpportunityException extends RuntimeException {

    private final int statusCode;

    protected OpportunityException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}