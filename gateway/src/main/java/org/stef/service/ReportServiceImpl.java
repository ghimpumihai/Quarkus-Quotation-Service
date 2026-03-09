package org.stef.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.stef.client.ReportRestClient;
import org.stef.dto.OpportunityDTO;
import org.stef.utils.CSVHelper;

import java.io.ByteArrayInputStream;
import java.util.List;

@ApplicationScoped
public class ReportServiceImpl implements ReportService {

    @Inject
    @RestClient
    ReportRestClient reportRestClient;

    @Override
    public ByteArrayInputStream generateCSVOpportunityReport() {
        List<OpportunityDTO> opportunityData = reportRestClient.requestOpportunityData();
        return CSVHelper.opportunitiesToCSV(opportunityData);
    }

    @Override
    public List<OpportunityDTO> getOppotunitiesData() {
        return reportRestClient.requestOpportunityData();
    }
}
