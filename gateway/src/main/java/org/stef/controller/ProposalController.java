package org.stef.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.service.ProposalService;

@ApplicationScoped
@Path("/api/proposal")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProposalController {

    private final ProposalService proposalService;

    @Inject
    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @GET
    @Path("/{id}")
    @RolesAllowed({"manager", "user"})
    public Response getProposalDetailsById(@PathParam("id") Long id) {
        return Response.ok(proposalService.getProposalDetailsById(id)).build();
    }

    @POST
    @RolesAllowed("proposal-customer")
    public Response createNewProposal(ProposalDetailsDTO proposalDetailsDTO) {
        proposalService.createProposal(proposalDetailsDTO);
        return Response.status(Response.Status.CREATED).build();
    }

    @DELETE
    @Path("/remove/{id}")
    @RolesAllowed("manager")
    public Response removeProposal(@PathParam("id") Long id) {
        proposalService.removeProposal(id);
        return Response.noContent().build();
    }
}