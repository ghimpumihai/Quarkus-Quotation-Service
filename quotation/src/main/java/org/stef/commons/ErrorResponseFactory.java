package org.stef.commons;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.stef.commons.handler.QuotationExceptionHandler;
import org.stef.exception.QuotationException;

@ApplicationScoped
public class ErrorResponseFactory {

    @Inject
    Instance<QuotationExceptionHandler<?>> handlers;

    @SuppressWarnings("unchecked")
    public ErrorResponse create(QuotationException e) {
        return handlers.stream()
                .filter(h -> h.handles().equals(e.getClass()))
                .findFirst()
                .map(h -> ((QuotationExceptionHandler<QuotationException>) h).toErrorResponse(e))
                .orElse(new ErrorResponse(e.getStatusCode(), "Internal Server Error", e.getMessage()));
    }
}