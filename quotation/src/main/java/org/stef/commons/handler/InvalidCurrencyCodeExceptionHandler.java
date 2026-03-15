package org.stef.commons.handler;

import jakarta.enterprise.context.ApplicationScoped;
import org.stef.commons.ErrorResponse;
import org.stef.exception.InvalidCurrencyCodeException;

@ApplicationScoped
public class InvalidCurrencyCodeExceptionHandler
        implements QuotationExceptionHandler<InvalidCurrencyCodeException> {

    @Override
    public Class<InvalidCurrencyCodeException> handles() {
        return InvalidCurrencyCodeException.class;
    }

    @Override
    public ErrorResponse toErrorResponse(InvalidCurrencyCodeException e) {
        return new ErrorResponse(e.getStatusCode(), "Bad Request", e.getMessage());
    }
}