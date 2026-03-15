package org.stef.exception;

public class ProviderUnavailableException extends QuotationException {

    public ProviderUnavailableException(String message) {
        super(message, 503);
    }

    public ProviderUnavailableException(String message, Throwable cause) {
        super(message, 503, cause);
    }
}