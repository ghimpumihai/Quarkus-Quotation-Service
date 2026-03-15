package org.stef.commons;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.stef.commons.handler.ProposalExceptionHandler;
import org.stef.exception.ProposalException;

@ApplicationScoped
public class ErrorResponseFactory {

    @Inject
    Instance<ProposalExceptionHandler<?>> handlers;

    @SuppressWarnings("unchecked")
    public ErrorResponse create(ProposalException e) {
        return handlers.stream()
                .filter(h -> h.handles().equals(e.getClass()))
                .findFirst()
                .map(h -> ((ProposalExceptionHandler<ProposalException>) h).toErrorResponse(e))
                .orElse(new ErrorResponse(e.getStatusCode(), "Internal Server Error", e.getMessage()));
    }
}