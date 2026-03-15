package org.stef.commons;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.stef.exception.ProposalException;

@Provider
public class ProposalExceptionMapper implements ExceptionMapper<ProposalException> {

    @Inject
    ErrorResponseFactory errorResponseFactory;

    @Override
    public Response toResponse(ProposalException e) {
        return Response
                .status(e.getStatusCode())
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponseFactory.create(e))
                .build();
    }
}