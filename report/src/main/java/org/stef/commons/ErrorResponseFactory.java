// commons/ErrorResponseFactory.java
package org.stef.commons;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.stef.commons.handler.OpportunityExceptionHandler;
import org.stef.exception.OpportunityException;

@ApplicationScoped
public class ErrorResponseFactory {

    @Inject
    Instance<OpportunityExceptionHandler<?>> handlers;

    @SuppressWarnings("unchecked")
    public ErrorResponse create(OpportunityException e) {
        return handlers.stream()
                .filter(h -> h.handles().equals(e.getClass()))
                .findFirst()
                .map(h -> ((OpportunityExceptionHandler<OpportunityException>) h).toErrorResponse(e))
                .orElse(new ErrorResponse(e.getStatusCode(), "Internal Server Error", e.getMessage()));
    }
}