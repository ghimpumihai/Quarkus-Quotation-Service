package org.stef.exception;

public class ProposalCreationException extends ProposalException {

    public ProposalCreationException(String message, Throwable cause) {
        super(message, 500, cause);
    }
}