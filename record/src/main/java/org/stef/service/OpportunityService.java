package org.stef.service;


import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface OpportunityService {
    void buildOpportunity(ProposalDTO proposalDTO);

    void saveQuotation(QuotationDTO quotationDTO);

    ByteArrayInputStream generateCSVOpportunityReport();
}
