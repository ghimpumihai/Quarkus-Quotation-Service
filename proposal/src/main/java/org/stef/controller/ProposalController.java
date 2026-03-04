package org.stef.controller;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.service.ProposalService;

@Path("/api/proposal")
public class ProposalController {

    private final Logger LOG = Logger.getLogger(ProposalController.class);

    @Inject
    ProposalService proposalService;

    @GET
    @Path("/{id}")
    public ProposalDetailsDTO findDetailsProposal(@PathParam("id") Long id) {
        return proposalService.findFullProposal(id);
    }

    @POST
    public Response createProposal(ProposalDetailsDTO proposalDetailsDTO) {
        try {
            proposalService.createProposal(proposalDetailsDTO);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("Error creating proposal", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create proposal: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProposal(@PathParam("id") Long id) {
        try {
            proposalService.removeProposal(id);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.errorf(e, "Error deleting proposal with id %d", id);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete proposal: " + e.getMessage())
                    .build();
        }
    }
}
