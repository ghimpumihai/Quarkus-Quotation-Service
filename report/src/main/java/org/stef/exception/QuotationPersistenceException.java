package org.stef.exception;

public class QuotationPersistenceException extends OpportunityException {

    public QuotationPersistenceException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

}

