package org.stef.commons.handler;

import org.stef.commons.ErrorResponse;
import org.stef.exception.GatewayException;

public interface GatewayExceptionHandler<T extends GatewayException> {
    Class<T> handles();
    ErrorResponse toErrorResponse(T exception);
}