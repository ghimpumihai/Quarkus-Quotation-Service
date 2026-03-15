package org.stef.exception;

public class ReportGenerationException extends OpportunityException {

    public ReportGenerationException(String message, Throwable cause) {
        super(message, 500, cause);
    }
}

