package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.controller.ProposalController;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.service.ProposalService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProposalControllerTest {

    @Test
    void createProposalReturnsOkWhenServiceSucceeds() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalController controller = new ProposalController(proposalService);

        try (var response = controller.createProposal(ProposalDetailsDTO.builder().customer("ACME").build())) {
            assertEquals(200, response.getStatus());
        }
        verify(proposalService).createProposal(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void findDetailsDelegatesToService() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalDetailsDTO dto = ProposalDetailsDTO.builder().proposalId(7L).customer("ACME").build();
        org.mockito.Mockito.when(proposalService.findFullProposal(7L)).thenReturn(dto);
        ProposalController controller = new ProposalController(proposalService);

        var response = controller.findDetailsProposal(7L);

        assertEquals(dto, response);
    }

    @Test
    void deleteProposalReturnsServerErrorWhenServiceFails() {
        ProposalService proposalService = mock(ProposalService.class);
        doThrow(new RuntimeException("boom")).when(proposalService).removeProposal(42L);
        ProposalController controller = new ProposalController(proposalService);

        try (var response = controller.deleteProposal(42L)) {
            assertEquals(500, response.getStatus());
        }
    }
}
