package org.stef;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.stef.dto.ProposalDTO;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.entity.Proposal;
import org.stef.exception.ProposalCreationException;
import org.stef.exception.ProposalNotFoundException;
import org.stef.message.KafkaEvent;
import org.stef.repository.ProposalRepository;
import org.stef.service.ProposalServiceImpl;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProposalServiceImplUnitTest {

    @Test
    void findFullProposalMapsEntityToDto() {
        ProposalRepository repository = mock(ProposalRepository.class);
        KafkaEvent kafkaEvent = mock(KafkaEvent.class);
        ProposalServiceImpl service = new ProposalServiceImpl(repository, kafkaEvent);

        Proposal proposal = Proposal.builder()
                .id(12L)
                .customer("ACME")
                .priceTonne(new BigDecimal("30.50"))
                .tonnes(3)
                .country("BR")
                .proposalValidityDays(7)
                .build();
        when(repository.findById(12L)).thenReturn(proposal);

        ProposalDetailsDTO result = service.findFullProposal(12L);

        assertEquals(12L, result.proposalId());
        assertEquals("ACME", result.customer());
        assertEquals(new BigDecimal("30.50"), result.priceTonne());
        assertEquals(3, result.tonnes());
        assertEquals("BR", result.country());
        assertEquals(7, result.proposalValidityDays());
    }

    @Test
    void createProposalPersistsAndPublishesProposalEvent() {
        ProposalRepository repository = mock(ProposalRepository.class);
        KafkaEvent kafkaEvent = mock(KafkaEvent.class);
        ProposalServiceImpl service = new ProposalServiceImpl(repository, kafkaEvent);
        ProposalDetailsDTO dto = ProposalDetailsDTO.builder()
                .customer("Client")
                .priceTonne(new BigDecimal("22.10"))
                .tonnes(8)
                .country("PT")
                .proposalValidityDays(12)
                .build();

        service.createProposal(dto);

        ArgumentCaptor<Proposal> proposalCaptor = ArgumentCaptor.forClass(Proposal.class);
        verify(repository).persist(proposalCaptor.capture());
        Proposal persisted = proposalCaptor.getValue();
        assertEquals("Client", persisted.getCustomer());
        assertEquals(new BigDecimal("22.10"), persisted.getPriceTonne());
        assertEquals(8, persisted.getTonnes());
        assertEquals("PT", persisted.getCountry());
        assertEquals(12, persisted.getProposalValidityDays());

        ArgumentCaptor<ProposalDTO> eventCaptor = ArgumentCaptor.forClass(ProposalDTO.class);
        verify(kafkaEvent).sendProposalRequest(eventCaptor.capture());
        ProposalDTO sent = eventCaptor.getValue();
        assertEquals("Client", sent.customer());
        assertEquals(new BigDecimal("22.10"), sent.priceTonne());
    }

    @Test
    void createProposalWrapsPersistenceException() {
        ProposalRepository repository = mock(ProposalRepository.class);
        KafkaEvent kafkaEvent = mock(KafkaEvent.class);
        ProposalServiceImpl service = new ProposalServiceImpl(repository, kafkaEvent);
        ProposalDetailsDTO dto = ProposalDetailsDTO.builder().customer("Broken").build();
        doThrow(new PersistenceException("db down")).when(repository).persist(any(Proposal.class));

        ProposalCreationException exception = assertThrows(ProposalCreationException.class,
                () -> service.createProposal(dto));

        assertTrue(exception.getMessage().contains("Failed to persist proposal"));
        assertEquals(500, exception.getStatusCode());
        verify(kafkaEvent, never()).sendProposalRequest(any());
    }

    @Test
    void createProposalWrapsUnexpectedException() {
        ProposalRepository repository = mock(ProposalRepository.class);
        KafkaEvent kafkaEvent = mock(KafkaEvent.class);
        ProposalServiceImpl service = new ProposalServiceImpl(repository, kafkaEvent);
        ProposalDetailsDTO dto = ProposalDetailsDTO.builder().customer("Broken").build();
        doThrow(new IllegalStateException("boom")).when(repository).persist(any(Proposal.class));

        ProposalCreationException exception = assertThrows(ProposalCreationException.class,
                () -> service.createProposal(dto));

        assertTrue(exception.getMessage().contains("Unexpected error creating proposal"));
        assertEquals(500, exception.getStatusCode());
    }

    @Test
    void findFullProposalThrowsNotFoundWhenProposalDoesNotExist() {
        ProposalRepository repository = mock(ProposalRepository.class);
        KafkaEvent kafkaEvent = mock(KafkaEvent.class);
        ProposalServiceImpl service = new ProposalServiceImpl(repository, kafkaEvent);
        when(repository.findById(99L)).thenReturn(null);

        ProposalNotFoundException exception = assertThrows(ProposalNotFoundException.class,
                () -> service.findFullProposal(99L));

        assertEquals("Proposal not found with id: 99", exception.getMessage());
        assertEquals(404, exception.getStatusCode());
    }

    @Test
    void removeProposalDeletesById() {
        ProposalRepository repository = mock(ProposalRepository.class);
        KafkaEvent kafkaEvent = mock(KafkaEvent.class);
        ProposalServiceImpl service = new ProposalServiceImpl(repository, kafkaEvent);

        service.removeProposal(99L);

        verify(repository).deleteById(99L);
    }
}
