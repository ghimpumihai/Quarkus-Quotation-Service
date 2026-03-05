package org.stef.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.stef.service.OpportunityServiceImpl;

import java.util.Date;

@Path("/api/opportunities")
public class OpportunityController {

    @Inject
    OpportunityServiceImpl opportunityService;

    @GET
    @Path("/report")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response generateReport() {
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
}
