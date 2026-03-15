package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.DownstreamNotFoundException;

@ApplicationScoped
public class DownstreamNotFoundExceptionHandler
        implements GatewayExceptionHandler<DownstreamNotFoundException> {

    @Override
    public Class<DownstreamNotFoundException> handles() {
        return DownstreamNotFoundException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(DownstreamNotFoundException e) {
        return new ErrorResponse(e.getStatusCode(), "Not Found", e.getMessage());
    }
}