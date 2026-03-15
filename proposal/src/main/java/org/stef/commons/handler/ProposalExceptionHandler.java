package org.stef.commons.handler;

import org.stef.commons.ErrorResponse;
import org.stef.exception.ProposalException;

public interface ProposalExceptionHandler<T extends ProposalException> {
    Class<T> handles();
    ErrorResponse toErrorResponse(T exception);
}