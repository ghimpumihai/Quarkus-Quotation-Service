package org.stef;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.stef.exception.InvalidCurrencyCodeException;
import org.stef.exception.ProviderUnavailableException;

@Path("/test/exception")
@ApplicationScoped
public class QuotationExceptionTestResource {

    @GET
    @Path("/provider")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerProviderUnavailable() {
        throw new ProviderUnavailableException("Currency API is unavailable (HTTP 503)");
    }

    @GET
    @Path("/currency")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerInvalidCurrency() {
        throw new InvalidCurrencyCodeException("USD-BRL");
    }

}
