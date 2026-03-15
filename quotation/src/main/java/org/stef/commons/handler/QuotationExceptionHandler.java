// commons/QuotationExceptionHandler.java
package org.stef.commons.handler;

import org.stef.commons.ErrorResponse;
import org.stef.exception.QuotationException;

public interface QuotationExceptionHandler<T extends QuotationException> {
    Class<T> handles();
    ErrorResponse toErrorResponse(T exception);
}