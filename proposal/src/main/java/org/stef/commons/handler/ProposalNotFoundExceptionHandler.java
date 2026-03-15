package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.ProposalNotFoundException;

@ApplicationScoped
public class ProposalNotFoundExceptionHandler
        implements ProposalExceptionHandler<ProposalNotFoundException> {

    @Override
    public Class<ProposalNotFoundException> handles() {
        return ProposalNotFoundException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(ProposalNotFoundException e) {
        return new ErrorResponse(e.getStatusCode(), "Not Found", e.getMessage());
    }
}