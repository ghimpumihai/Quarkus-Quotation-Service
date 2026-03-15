package org.stef.exception;

import lombok.Getter;

@Getter
public abstract class QuotationException extends RuntimeException {

    private final int statusCode;

    protected QuotationException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    protected QuotationException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}