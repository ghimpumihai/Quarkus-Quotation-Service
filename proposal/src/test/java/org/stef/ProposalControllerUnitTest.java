package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.controller.ProposalController;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.service.ProposalService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProposalControllerUnitTest {

    @Test
    void createProposalReturnsCreatedWhenServiceSucceeds() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalController controller = new ProposalController(proposalService);

        try (var response = controller.createProposal(ProposalDetailsDTO.builder().customer("ACME").build())) {
            assertEquals(201, response.getStatus());
        }
        verify(proposalService).createProposal(any());
    }

    @Test
    void deleteProposalReturnsNoContentWhenServiceSucceeds() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalController controller = new ProposalController(proposalService);

        try (var response = controller.deleteProposal(42L)) {
            assertEquals(204, response.getStatus());
        }
        verify(proposalService).removeProposal(42L);
    }

    @Test
    void findDetailsDelegatesToService() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalDetailsDTO dto = ProposalDetailsDTO.builder().proposalId(7L).customer("ACME").build();
        when(proposalService.findFullProposal(7L)).thenReturn(dto);
        ProposalController controller = new ProposalController(proposalService);

        var result = controller.findDetailsProposal(7L);

        assertEquals(dto, result);
    }
}
