package org.stef.commons.handler;

import org.stef.commons.ErrorResponse;
import org.stef.exception.OpportunityException;

public interface OpportunityExceptionHandler<T extends OpportunityException> {
    Class<T> handles();
    ErrorResponse toErrorResponse(T exception);
}