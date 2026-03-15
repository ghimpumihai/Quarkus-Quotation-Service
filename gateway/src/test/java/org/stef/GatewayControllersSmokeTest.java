package org.stef;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.stef.controller.ProposalController;
import org.stef.controller.ReportController;
import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.service.ProposalService;
import org.stef.service.ReportService;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GatewayControllersSmokeTest {

    @Test
    void proposalControllerReturnsProposalDetails() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalDetailsDTO dto = ProposalDetailsDTO.builder()
                .proposalId(9L)
                .customer("ACME")
                .priceTonne(new BigDecimal("120.50"))
                .tonnes(4)
                .country("BR")
                .proposalValidityDays(15)
                .build();
        when(proposalService.getProposalDetailsById(9L)).thenReturn(dto);
        ProposalController controller = new ProposalController(proposalService);

        try (Response response = controller.getProposalDetailsById(9L)) {
            assertEquals(200, response.getStatus());
            assertEquals(dto, response.getEntity());
        }
    }

    @Test
    void proposalControllerReturnsCreatedOnSuccess() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalController controller = new ProposalController(proposalService);

        try (Response response = controller.createNewProposal(ProposalDetailsDTO.builder().customer("ACME").build())) {
            assertEquals(201, response.getStatus());
        }
        verify(proposalService).createProposal(any());
    }

    @Test
    void proposalControllerReturnsNoContentOnDelete() {
        ProposalService proposalService = mock(ProposalService.class);
        ProposalController controller = new ProposalController(proposalService);

        try (Response response = controller.removeProposal(5L)) {
            assertEquals(204, response.getStatus());
        }
        verify(proposalService).removeProposal(5L);
    }

    @Test
    void reportControllerReturnsCsvStreamWithAttachmentHeader() {
        ReportService reportService = mock(ReportService.class);
        byte[] csvBytes = "header\nvalue".getBytes(StandardCharsets.UTF_8);
        when(reportService.generateCSVOpportunityReport()).thenReturn(new ByteArrayInputStream(csvBytes));
        ReportController controller = new ReportController(reportService);

        try (Response response = controller.generateReport()) {
            assertEquals(200, response.getStatus());
            assertNotNull(response.getHeaderString("content-disposition"));
            assertTrue(response.getHeaderString("content-disposition").contains("_opportunity_report.csv"));
            assertInstanceOf(ByteArrayInputStream.class, response.getEntity());
        }
    }

    @Test
    void reportControllerReturnsOpportunityData() {
        ReportService reportService = mock(ReportService.class);
        List<OpportunityDTO> expected = List.of(OpportunityDTO.builder()
                .proposalId(1L)
                .customer("ACME")
                .priceTonne(new BigDecimal("10.0"))
                .lastCurrencyQuotation(new BigDecimal("5.0"))
                .build());
        when(reportService.getOpportunitiesData()).thenReturn(expected);
        ReportController controller = new ReportController(reportService);

        try (Response response = controller.requestOpportunityData()) {
            assertEquals(200, response.getStatus());
            assertEquals(expected, response.getEntity());
        }
    }
}

