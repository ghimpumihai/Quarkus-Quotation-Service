package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;
import org.stef.message.KafkaEvent;
import org.stef.service.OpportunityService;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReportKafkaEventTest {

    @Test
    void receiveProposalDelegatesToOpportunityService() {
        OpportunityService opportunityService = mock(OpportunityService.class);
        KafkaEvent kafkaEvent = new KafkaEvent(opportunityService);
        ProposalDTO proposalDTO = ProposalDTO.builder()
                .proposalId(15L)
                .customer("ACME")
                .priceTonne(new BigDecimal("20.00"))
                .build();

        kafkaEvent.receiveProposal(proposalDTO);

        verify(opportunityService).buildOpportunity(proposalDTO);
    }

    @Test
    void receiveQuotationDelegatesToOpportunityService() {
        OpportunityService opportunityService = mock(OpportunityService.class);
        KafkaEvent kafkaEvent = new KafkaEvent(opportunityService);
        QuotationDTO quotationDTO = new QuotationDTO(null, new BigDecimal("5.20"));

        kafkaEvent.receiveQuotation(quotationDTO);

        verify(opportunityService).saveQuotation(quotationDTO);
    }
}
