package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.ProviderUnavailableException;

@ApplicationScoped
public class ProviderUnavailableExceptionHandler
        implements QuotationExceptionHandler<ProviderUnavailableException> {

    @Override
    public Class<ProviderUnavailableException> handles() {
        return ProviderUnavailableException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(ProviderUnavailableException e) {
        return new ErrorResponse(e.getStatusCode(), "Service Unavailable", e.getMessage());
    }
}