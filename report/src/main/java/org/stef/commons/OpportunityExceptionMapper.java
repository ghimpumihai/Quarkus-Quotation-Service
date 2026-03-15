package org.stef.commons;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.stef.exception.OpportunityException;

@Provider
public class OpportunityExceptionMapper implements ExceptionMapper<OpportunityException> {

    @Inject
    ErrorResponseFactory errorResponseFactory;

    @Override
    public Response toResponse(OpportunityException e) {
        return Response
                .status(e.getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponseFactory.create(e))
                .build();
    }
}