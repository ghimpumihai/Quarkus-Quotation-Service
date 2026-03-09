package org.stef.controller;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.stef.dto.OpportunityDTO;
import org.stef.service.OpportunityService;

import java.util.List;

@Path("/api/report")
@Authenticated
public class OpportunityController {

    private final OpportunityService opportunityService;

    @Inject
    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GET
    @Path("/data")
    @RolesAllowed({"manager","user"})
    public List<OpportunityDTO> generateReport() {
        return opportunityService.generateOpportunityReport();
    }
}
