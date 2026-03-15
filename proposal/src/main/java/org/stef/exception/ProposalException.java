// exception/ProposalException.java
package org.stef.exception;

import lombok.Getter;

@Getter
public abstract class ProposalException extends RuntimeException {

    private final int statusCode;

    protected ProposalException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    protected ProposalException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}