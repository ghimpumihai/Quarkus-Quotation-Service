package org.stef.message;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;
import org.stef.service.OpportunityService;
import org.stef.service.OpportunityServiceImpl;

public class KafkaEvent {

    private final Logger LOG = LoggerFactory.getLogger(KafkaEvent.class);

    @ApplicationScoped
    OpportunityServiceImpl opportunityService;

    @Incoming("proposal")
    @Transactional
    public void receiveProposal(ProposalDTO proposalDTO) {
        LOG.info("Received proposal: {}", proposalDTO);
        opportunityService.buildOpportunity(proposalDTO);
    }

    @Incoming("quotation")
    @Transactional
    public void receiveQuotation(QuotationDTO quotationDTO) {
        LOG.info("Received quotation: {}", quotationDTO);
        opportunityService.saveQuotation(quotationDTO);
    }
}
