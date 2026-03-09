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
    @Inject
    ProposalService proposalService;

    @GET
    @Path("/{id}")
    @RolesAllowed({"manager","user"})
    public Response getProposalDetailsById(@PathParam("id") Long id) {
        try{
            return Response.ok(proposalService.getProposalDetailsById(id),MediaType.APPLICATION_JSON).build();
        }
        catch(ServerErrorException e){
            return Response.serverError().build();
        }
    }

    @POST
    @RolesAllowed("proposal-customer")
    public Response createNewProposal(ProposalDetailsDTO proposalDetailsDTO){
        int proposalRequestStatus = proposalService.createProposal(proposalDetailsDTO).getStatus();
        if(proposalRequestStatus > 199 && proposalRequestStatus < 205){
            return Response.ok().build();
        }
        else{
            return Response.status(proposalRequestStatus).build();
        }
    }

    @DELETE
    @Path("/remove/{id}")
    @RolesAllowed("manager")
    public Response removeProposal(@PathParam("id") Long id){
        int proposalRequestStatus = proposalService.removeProposal(id).getStatus();
        if(proposalRequestStatus > 199 && proposalRequestStatus < 205){
            return Response.ok().build();
        }
        else{
            return Response.status(proposalRequestStatus).build();
        }
    }
}
