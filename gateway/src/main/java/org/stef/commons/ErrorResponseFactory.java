package org.stef.commons;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.stef.commons.handler.GatewayExceptionHandler;
import org.stef.exception.GatewayException;

@ApplicationScoped
public class ErrorResponseFactory {

    @Inject
    Instance<GatewayExceptionHandler<?>> handlers;

    @SuppressWarnings("unchecked")
    public ErrorResponse create(GatewayException e) {
        return handlers.stream()
                .filter(h -> h.handles().equals(e.getClass()))
                .findFirst()
                .map(h -> ((GatewayExceptionHandler<GatewayException>) h).toErrorResponse(e))
                .orElse(new ErrorResponse(e.getStatusCode(), "Internal Server Error", e.getMessage()));
    }
}