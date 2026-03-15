package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.ProposalCreationException;

@ApplicationScoped
public class ProposalCreationExceptionHandler
        implements ProposalExceptionHandler<ProposalCreationException> {

    @Override
    public Class<ProposalCreationException> handles() {
        return ProposalCreationException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(ProposalCreationException e) {
        return new ErrorResponse(e.getStatusCode(), "Internal Server Error", e.getMessage());
    }
}