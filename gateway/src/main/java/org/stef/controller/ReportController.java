package org.stef.controller;


import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.stef.service.ReportService;

import java.util.Date;

@ApplicationScoped
@Path("/api/report")
public class ReportController {

    private final ReportService reportService;

    @Inject
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({"manager", "user"})
    public Response generateReport() {
        return Response.ok(reportService.generateCSVOpportunityReport(), MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition", "attachment; filename=" + new Date() + "_opportunity_report.csv")
                .build();
    }

    @GET
    @Path("/data")
    @RolesAllowed({"manager", "user"})
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestOpportunityData() {
        return Response.ok(reportService.getOpportunitiesData()).build();
    }
}
