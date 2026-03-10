package org.stef.service;


import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;

import java.util.List;

public interface OpportunityService {
    void buildOpportunity(ProposalDTO proposalDTO);

    void saveQuotation(QuotationDTO quotationDTO);

    List<OpportunityDTO> generateOpportunityReport();
}
