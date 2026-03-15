package org.stef.client;

import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.stef.dto.ProposalDetailsDTO;


@Path("/api/proposal")
@RegisterRestClient
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
@RegisterProvider(GatewayResponseExceptionMapper.class)
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface ProposalRestClient {

    @GET
    @Path("/{id}")
    ProposalDetailsDTO getProposalDetailsById(@PathParam("id") Long id);

    @POST
    void createProposal(ProposalDetailsDTO proposalDetailsDTO);

    @DELETE
    @Path("/remove/{id}")
    void deleteProposal(@PathParam("id") Long id);
}
