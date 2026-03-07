package org.stef.controller;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.stef.dto.OpportunityDTO;
import org.stef.service.OpportunityServiceImpl;

import java.util.Date;
import java.util.List;

@Path("/api/opportunities")
@Authenticated
public class OpportunityController {

    @Inject
    OpportunityServiceImpl opportunityService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Path("/report/csv")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"manager","user"})
    public Response generateReportCSV() {
        try{
            return Response.ok(opportunityService.generateCSVOpportunityReport())
                    .header("Content-Disposition", "attachment; filename=" + new Date() + "_opportunity_report.csv")
                    .build();
        }
        catch(ServerErrorException e){
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while generating the report: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/report")
    @RolesAllowed({"manager","user"})
    public List<OpportunityDTO> generateReport() {
        return opportunityService.generateOpportunityReport();
    }
}
