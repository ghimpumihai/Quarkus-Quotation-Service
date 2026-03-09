package org.stef.controller;


import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.stef.service.ReportService;

import java.util.Date;

@ApplicationScoped
@Path("/api/report")
public class ReportController {

    @Inject
    ReportService reportService;

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"manager","user"})
    public Response generateReport() {
        try{
            return Response.ok(reportService.generateCSVOpportunityReport(),MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition", "attachment; filename=" + new Date() + "_opportunity_report.csv")
                    .build();
        }
        catch(ServerErrorException e){
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/data")
    @RolesAllowed({"manager","user"})
    public Response requestOpportunityData() {
        try{
            return Response.ok(reportService.getOppotunitiesData(),MediaType.APPLICATION_JSON).build();
        }
        catch(ServerErrorException e){
            return Response.serverError().build();
        }
    }
}
