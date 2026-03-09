package org.stef.controller;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.stef.dto.OpportunityDTO;
import org.stef.service.OpportunityServiceImpl;

import java.util.List;

@Path("/api/report")
@Authenticated
public class OpportunityController {

    @Inject
    OpportunityServiceImpl opportunityService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/data")
    @RolesAllowed({"manager","user"})
    public List<OpportunityDTO> generateReport() {
        return opportunityService.generateOpportunityReport();
    }
}
