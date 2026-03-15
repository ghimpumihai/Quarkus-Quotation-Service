package org.stef;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.stef.exception.ProposalCreationException;
import org.stef.exception.ProposalNotFoundException;

@Path("/test/proposal/exception")
@ApplicationScoped
public class ProposalExceptionTestResource {

    @GET
    @Path("/not-found")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerNotFound() {
        throw new ProposalNotFoundException(99L);
    }

    @GET
    @Path("/creation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response triggerCreationFailed() {
        throw new ProposalCreationException("Failed to persist proposal",
                new RuntimeException("DB connection lost"));
    }
}