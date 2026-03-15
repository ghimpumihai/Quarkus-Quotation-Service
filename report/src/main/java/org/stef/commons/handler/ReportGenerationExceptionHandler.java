package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.ReportGenerationException;

@ApplicationScoped
public class ReportGenerationExceptionHandler
        implements OpportunityExceptionHandler<ReportGenerationException> {

    @Override
    public Class<ReportGenerationException> handles() {
        return ReportGenerationException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(ReportGenerationException e) {
        return new ErrorResponse(e.getStatusCode(), "Internal Server Error", e.getMessage());
    }
}