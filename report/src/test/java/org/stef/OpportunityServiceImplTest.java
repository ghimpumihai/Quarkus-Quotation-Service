package org.stef;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;
import org.stef.entity.Opportunity;
import org.stef.entity.Quotation;
import org.stef.exception.QuotationPersistenceException;
import org.stef.exception.ReportGenerationException;
import org.stef.repository.OpportunitiesRepository;
import org.stef.repository.QuotationRepository;
import org.stef.service.OpportunityServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpportunityServiceImplTest {

    @Test
    void buildOpportunityUsesLastQuotationWhenAvailable() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> quotationQuery = mock(PanacheQuery.class);
        Quotation quotation = new Quotation();
        quotation.setCurrencyPrice(new BigDecimal("5.12"));
        when(quotationRepository.findAll()).thenReturn(quotationQuery);
        when(quotationQuery.list()).thenReturn(List.of(quotation));
        ProposalDTO proposal = ProposalDTO.builder()
                .proposalId(1L)
                .customer("ACME")
                .priceTonne(new BigDecimal("20.00"))
                .build();

        service.buildOpportunity(proposal);

        ArgumentCaptor<Opportunity> opportunityCaptor = ArgumentCaptor.forClass(Opportunity.class);
        verify(opportunitiesRepository).persist(opportunityCaptor.capture());
        Opportunity saved = opportunityCaptor.getValue();
        assertEquals(1L, saved.getProposalId());
        assertEquals("ACME", saved.getCustomer());
        assertEquals(new BigDecimal("20.00"), saved.getPriceTonne());
        assertEquals(new BigDecimal("5.12"), saved.getLastCurrencyQuotation());
        assertNotNull(saved.getDate());
    }

    @Test
    void buildOpportunityFallsBackToZeroWhenNoQuotationExists() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> quotationQuery = mock(PanacheQuery.class);
        when(quotationRepository.findAll()).thenReturn(quotationQuery);
        when(quotationQuery.list()).thenReturn(List.of());
        ProposalDTO proposal = ProposalDTO.builder()
                .proposalId(2L)
                .customer("Globex")
                .priceTonne(new BigDecimal("30.00"))
                .build();

        service.buildOpportunity(proposal);

        ArgumentCaptor<Opportunity> opportunityCaptor = ArgumentCaptor.forClass(Opportunity.class);
        verify(opportunitiesRepository).persist(opportunityCaptor.capture());
        assertEquals(0, BigDecimal.ZERO.compareTo(opportunityCaptor.getValue().getLastCurrencyQuotation()));
    }

    @Test
    void saveQuotationPersistsCurrencyValue() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        QuotationDTO quotationDTO = new QuotationDTO(null, new BigDecimal("5.55"));

        service.saveQuotation(quotationDTO);

        ArgumentCaptor<Quotation> quotationCaptor = ArgumentCaptor.forClass(Quotation.class);
        verify(quotationRepository).persist(quotationCaptor.capture());
        assertEquals(new BigDecimal("5.55"), quotationCaptor.getValue().getCurrencyPrice());
        assertNotNull(quotationCaptor.getValue().getDate());
    }

    @Test
    void generateOpportunityReportMapsEntitiesToDtos() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Opportunity> opportunityQuery = mock(PanacheQuery.class);
        Opportunity first = new Opportunity();
        first.setProposalId(10L);
        first.setCustomer("A");
        first.setPriceTonne(new BigDecimal("40.0"));
        first.setLastCurrencyQuotation(new BigDecimal("5.0"));
        Opportunity second = new Opportunity();
        second.setProposalId(11L);
        second.setCustomer("B");
        second.setPriceTonne(new BigDecimal("41.0"));
        second.setLastCurrencyQuotation(new BigDecimal("5.1"));
        when(opportunitiesRepository.findAll()).thenReturn(opportunityQuery);
        when(opportunityQuery.stream()).thenReturn(Stream.of(first, second));

        List<OpportunityDTO> result = service.generateOpportunityReport();

        assertEquals(2, result.size());
        assertEquals(10L, result.get(0).proposalId());
        assertEquals("A", result.get(0).customer());
        assertEquals(new BigDecimal("41.0"), result.get(1).priceTonne());
        assertEquals(new BigDecimal("5.1"), result.get(1).lastCurrencyQuotation());
    }

    @Test
    void buildOpportunityThrowsQuotationPersistenceExceptionOnPersistenceFailure() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> quotationQuery = mock(PanacheQuery.class);
        when(quotationRepository.findAll()).thenReturn(quotationQuery);
        when(quotationQuery.list()).thenReturn(List.of());
        doThrow(new PersistenceException("db down")).when(opportunitiesRepository).persist(any(Opportunity.class));
        ProposalDTO dto = ProposalDTO.builder().proposalId(1L).customer("ACME").build();

        QuotationPersistenceException ex = assertThrows(QuotationPersistenceException.class,
                () -> service.buildOpportunity(dto));

        assertEquals("Failed to persist opportunity", ex.getMessage());
        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void buildOpportunityThrowsQuotationPersistenceExceptionOnUnexpectedError() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> quotationQuery = mock(PanacheQuery.class);
        when(quotationRepository.findAll()).thenReturn(quotationQuery);
        when(quotationQuery.list()).thenReturn(List.of());
        doThrow(new RuntimeException("unexpected")).when(opportunitiesRepository).persist(any(Opportunity.class));
        ProposalDTO dto = ProposalDTO.builder().proposalId(1L).customer("ACME").build();

        QuotationPersistenceException ex = assertThrows(QuotationPersistenceException.class,
                () -> service.buildOpportunity(dto));

        assertEquals("Unexpected error building opportunity", ex.getMessage());
        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void saveQuotationThrowsQuotationPersistenceExceptionOnPersistenceFailure() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        doThrow(new PersistenceException("db down")).when(quotationRepository).persist(any(Quotation.class));
        QuotationDTO dto = new QuotationDTO(null, new BigDecimal("5.55"));

        QuotationPersistenceException ex = assertThrows(QuotationPersistenceException.class,
                () -> service.saveQuotation(dto));

        assertEquals("Failed to persist quotation", ex.getMessage());
        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void saveQuotationThrowsQuotationPersistenceExceptionOnUnexpectedError() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        doThrow(new RuntimeException("unexpected")).when(quotationRepository).persist(any(Quotation.class));
        QuotationDTO dto = new QuotationDTO(null, new BigDecimal("5.55"));

        QuotationPersistenceException ex = assertThrows(QuotationPersistenceException.class,
                () -> service.saveQuotation(dto));

        assertEquals("Unexpected error saving quotation", ex.getMessage());
        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void generateOpportunityReportThrowsReportGenerationExceptionOnRepositoryFailure() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        when(opportunitiesRepository.findAll()).thenThrow(new RuntimeException("db down"));

        ReportGenerationException ex = assertThrows(ReportGenerationException.class,
                service::generateOpportunityReport);

        assertEquals("Failed to generate opportunity report", ex.getMessage());
        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void generateOpportunityReportSkipsNullEntities() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Opportunity> opportunityQuery = mock(PanacheQuery.class);
        Opportunity valid = mock(Opportunity.class);
        when(valid.getProposalId()).thenReturn(1L);
        when(valid.getCustomer()).thenReturn("ACME");
        when(valid.getPriceTonne()).thenReturn(new BigDecimal("20.00"));
        when(valid.getLastCurrencyQuotation()).thenReturn(new BigDecimal("5.00"));
        List<Opportunity> withNull = new ArrayList<>();
        withNull.add(null);
        withNull.add(valid);
        when(opportunitiesRepository.findAll()).thenReturn(opportunityQuery);
        when(opportunityQuery.stream()).thenReturn(withNull.stream());

        List<OpportunityDTO> result = service.generateOpportunityReport();

        assertEquals(1, result.size());
        assertEquals(1L, result.getFirst().proposalId());
    }

    @Test
    void generateOpportunityReportFallsBackToZeroWhenLastCurrencyQuotationIsNull() {
        QuotationRepository quotationRepository = mock(QuotationRepository.class);
        OpportunitiesRepository opportunitiesRepository = mock(OpportunitiesRepository.class);
        OpportunityServiceImpl service = new OpportunityServiceImpl(quotationRepository, opportunitiesRepository);
        @SuppressWarnings("unchecked")
        PanacheQuery<Opportunity> opportunityQuery = mock(PanacheQuery.class);
        Opportunity opportunity = mock(Opportunity.class);
        when(opportunity.getProposalId()).thenReturn(1L);
        when(opportunity.getCustomer()).thenReturn("ACME");
        when(opportunity.getPriceTonne()).thenReturn(new BigDecimal("20.00"));
        when(opportunity.getLastCurrencyQuotation()).thenReturn(null);
        when(opportunitiesRepository.findAll()).thenReturn(opportunityQuery);
        when(opportunityQuery.stream()).thenReturn(Stream.of(opportunity));

        List<OpportunityDTO> result = service.generateOpportunityReport();

        assertEquals(1, result.size());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getFirst().lastCurrencyQuotation()));
    }
}