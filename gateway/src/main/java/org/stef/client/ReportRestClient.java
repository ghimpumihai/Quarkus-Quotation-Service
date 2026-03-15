package org.stef.client;

import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.stef.dto.OpportunityDTO;

import java.util.List;

@Path("/api/report")
@RegisterRestClient
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProvider(GatewayResponseExceptionMapper.class)
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public interface ReportRestClient {
    @GET
    @Path("/data")
    List<OpportunityDTO> requestOpportunityData();

}
