package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.controller.OpportunityController;
import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;
import org.stef.service.OpportunityService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpportunityControllerTest {

    @Test
    void generateReportDelegatesToService() {
        List<OpportunityDTO> expected = List.of(OpportunityDTO.builder().proposalId(1L).customer("ACME").build());
        OpportunityService opportunityService = new StubOpportunityService(expected);
        OpportunityController controller = new OpportunityController(opportunityService);

        var result = controller.generateReport();

        assertEquals(expected, result);
    }

    private static final class StubOpportunityService implements OpportunityService {
        private final List<OpportunityDTO> report;

        private StubOpportunityService(List<OpportunityDTO> report) {
            this.report = report;
        }

        @Override
        public void buildOpportunity(ProposalDTO proposalDTO) {
        }

        @Override
        public void saveQuotation(QuotationDTO quotationDTO) {
        }

        @Override
        public List<OpportunityDTO> generateOpportunityReport() {
            return report;
        }
    }
}
