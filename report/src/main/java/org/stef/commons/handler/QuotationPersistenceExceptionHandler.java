package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.QuotationPersistenceException;

@ApplicationScoped
public class QuotationPersistenceExceptionHandler
        implements OpportunityExceptionHandler<QuotationPersistenceException> {

    @Override
    public Class<QuotationPersistenceException> handles() {
        return QuotationPersistenceException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(QuotationPersistenceException e) {
        return new ErrorResponse(e.getStatusCode(), "Internal Server Error", e.getMessage());
    }
}