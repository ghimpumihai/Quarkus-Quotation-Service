package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.DownstreamServiceException;

@ApplicationScoped
public class DownstreamServiceExceptionHandler
        implements GatewayExceptionHandler<DownstreamServiceException> {

    @Override
    public Class<DownstreamServiceException> handles() {
        return DownstreamServiceException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(DownstreamServiceException e) {
        return new ErrorResponse(e.getStatusCode(), "Service Unavailable", e.getMessage());
    }
}