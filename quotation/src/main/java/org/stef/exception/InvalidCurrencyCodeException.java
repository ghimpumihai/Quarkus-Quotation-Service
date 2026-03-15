package org.stef.exception;

public class InvalidCurrencyCodeException extends QuotationException {

    public InvalidCurrencyCodeException(String currencyCode) {
        super("Invalid or unsupported currency code: " + currencyCode, 400);
    }
}