package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.client.ProposalRestClient;
import org.stef.client.ReportRestClient;
import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.service.ProposalServiceImpl;
import org.stef.service.ReportServiceImpl;
import org.stef.utils.CSVHelper;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GatewayServiceAndUtilsTest {

    @Test
    void proposalServiceDelegatesToRestClient() {
        ProposalRestClient client = mock(ProposalRestClient.class);
        ProposalServiceImpl service = new ProposalServiceImpl(client);
        ProposalDetailsDTO dto = ProposalDetailsDTO.builder().proposalId(11L).customer("ACME").build();
        jakarta.ws.rs.core.Response createResponse = jakarta.ws.rs.core.Response.accepted().build();
        jakarta.ws.rs.core.Response deleteResponse = jakarta.ws.rs.core.Response.noContent().build();

        when(client.getProposalDetailsById(11L)).thenReturn(dto);
        when(client.createProposal(dto)).thenReturn(createResponse);
        when(client.deleteProposal(11L)).thenReturn(deleteResponse);

        assertSame(dto, service.getProposalDetailsById(11L));
        assertSame(createResponse, service.createProposal(dto));
        assertSame(deleteResponse, service.removeProposal(11L));

        verify(client).getProposalDetailsById(11L);
        verify(client).createProposal(dto);
        verify(client).deleteProposal(11L);
    }

    @Test
    void reportServiceBuildsCsvFromRestClientData() {
        ReportRestClient client = mock(ReportRestClient.class);
        ReportServiceImpl service = new ReportServiceImpl(client);
        List<OpportunityDTO> opportunities = List.of(OpportunityDTO.builder()
                .proposalId(7L)
                .customer("ACME")
                .priceTonne(new BigDecimal("44.10"))
                .lastCurrencyQuotation(new BigDecimal("5.22"))
                .build());
        when(client.requestOpportunityData()).thenReturn(opportunities);

        ByteArrayInputStream stream = service.generateCSVOpportunityReport();
        String csv = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(csv.contains("proposalId"));
        assertTrue(csv.contains("ACME"));
        assertTrue(csv.contains("44.10"));
        assertTrue(csv.contains("5.22"));
        assertEquals(opportunities, service.getOppotunitiesData());
        verify(client, times(2)).requestOpportunityData();
    }

    @Test
    void csvHelperSerializesHeadersAndRows() {
        ByteArrayInputStream stream = CSVHelper.opportunitiesToCSV(List.of(
                OpportunityDTO.builder()
                        .proposalId(1L)
                        .customer("Client A")
                        .priceTonne(new BigDecimal("99.99"))
                        .lastCurrencyQuotation(new BigDecimal("4.87"))
                        .build(),
                OpportunityDTO.builder()
                        .proposalId(2L)
                        .customer("Client B")
                        .priceTonne(new BigDecimal("50.00"))
                        .lastCurrencyQuotation(new BigDecimal("4.90"))
                        .build()
        ));

        String csv = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(csv.startsWith("proposalId,customer,priceTonne,tonnes,country,proposalValidityDays"));
        assertTrue(csv.contains("1,Client A,99.99,4.87"));
        assertTrue(csv.contains("2,Client B,50.00,4.90"));
    }
}
