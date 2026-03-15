package org.stef.service;


import org.stef.dto.OpportunityDTO;


import java.io.ByteArrayInputStream;
import java.util.List;

public interface ReportService {
    ByteArrayInputStream generateCSVOpportunityReport();
    List<OpportunityDTO> getOpportunitiesData();
}
