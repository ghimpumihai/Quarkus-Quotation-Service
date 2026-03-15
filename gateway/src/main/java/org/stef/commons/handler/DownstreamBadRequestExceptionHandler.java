package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.DownstreamBadRequestException;

@ApplicationScoped
public class DownstreamBadRequestExceptionHandler
        implements GatewayExceptionHandler<DownstreamBadRequestException> {

    @Override
    public Class<DownstreamBadRequestException> handles() {
        return DownstreamBadRequestException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(DownstreamBadRequestException e) {
        return new ErrorResponse(e.getStatusCode(), "Bad Request", e.getMessage());
    }
}