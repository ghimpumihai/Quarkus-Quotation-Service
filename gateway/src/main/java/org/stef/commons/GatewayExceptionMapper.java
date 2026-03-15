// commons/GatewayExceptionMapper.java
package org.stef.commons;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.stef.exception.GatewayException;

@Provider
public class GatewayExceptionMapper implements ExceptionMapper<GatewayException> {

    @Inject
    ErrorResponseFactory errorResponseFactory;

    @Override
    public Response toResponse(GatewayException e) {
        return Response
                .status(e.getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponseFactory.create(e))
                .build();
    }
}