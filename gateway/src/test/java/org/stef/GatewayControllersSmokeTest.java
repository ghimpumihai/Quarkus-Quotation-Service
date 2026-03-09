package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.controller.ProposalController;
import org.stef.controller.ReportController;
import org.stef.service.ProposalService;
import org.stef.service.ReportService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class GatewayControllersSmokeTest {

    @Test
    void proposalControllerCanBeConstructed() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalController controller = new ProposalController(proposalService);
        assertNotNull(controller);
    }

    @Test
    void reportControllerCanBeConstructed() {
        ReportService reportService = mock(ReportService.class);
        ReportController controller = new ReportController(reportService);
        assertNotNull(controller);
    }
}
