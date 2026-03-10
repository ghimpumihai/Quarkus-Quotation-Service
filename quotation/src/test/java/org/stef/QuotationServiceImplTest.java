package org.stef;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.stef.client.CurrencyPriceClient;
import org.stef.dto.CurrencyPriceDTO;
import org.stef.dto.QuotationDTO;
import org.stef.dto.USDBRL;
import org.stef.entity.Quotation;
import org.stef.message.KafkaEvents;
import org.stef.repository.QuotationRepository;
import org.stef.service.QuotationServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuotationServiceImplTest {

    @Test
    void getCurrencyPricePublishesEventWhenNoPreviousQuotationExists() {
        CurrencyPriceClient client = mock(CurrencyPriceClient.class);
        QuotationRepository repository = mock(QuotationRepository.class);
        KafkaEvents kafkaEvents = mock(KafkaEvents.class);
        QuotationServiceImpl service = new QuotationServiceImpl(client, repository, kafkaEvents);

        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> query = mock(PanacheQuery.class);

        CurrencyPriceDTO dto = new CurrencyPriceDTO(USDBRL.builder()
                .bid("5.10")
                .pctChange("0.2")
                .build());
        when(client.getPriceByPair("USD-BRL")).thenReturn(dto);
        when(repository.findAll()).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        service.getCurrencyPrice();

        verify(kafkaEvents).sendNewKafkaEvent(any());
        verify(repository).persist(any(Quotation.class));
    }

    @Test
    void getCurrencyPricePublishesEventWhenPriceIncreases() {
        CurrencyPriceClient client = mock(CurrencyPriceClient.class);
        QuotationRepository repository = mock(QuotationRepository.class);
        KafkaEvents kafkaEvents = mock(KafkaEvents.class);
        QuotationServiceImpl service = new QuotationServiceImpl(client, repository, kafkaEvents);
        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> query = mock(PanacheQuery.class);
        Quotation previous = new Quotation();
        previous.setCurrencyPrice(new BigDecimal("5.00"));

        CurrencyPriceDTO dto = new CurrencyPriceDTO(USDBRL.builder()
                .bid("5.20")
                .pctChange("0.8")
                .build());
        when(client.getPriceByPair("USD-BRL")).thenReturn(dto);
        when(repository.findAll()).thenReturn(query);
        when(query.list()).thenReturn(List.of(previous));

        service.getCurrencyPrice();

        ArgumentCaptor<QuotationDTO> eventCaptor = ArgumentCaptor.forClass(QuotationDTO.class);
        verify(kafkaEvents).sendNewKafkaEvent(eventCaptor.capture());
        assertEquals(new BigDecimal("5.20"), eventCaptor.getValue().currencyPrice());
        assertNotNull(eventCaptor.getValue().date());
        verify(repository).persist(any(Quotation.class));
    }

    @Test
    void getCurrencyPriceDoesNotPublishEventWhenPriceDoesNotIncrease() {
        CurrencyPriceClient client = mock(CurrencyPriceClient.class);
        QuotationRepository repository = mock(QuotationRepository.class);
        KafkaEvents kafkaEvents = mock(KafkaEvents.class);
        QuotationServiceImpl service = new QuotationServiceImpl(client, repository, kafkaEvents);
        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> query = mock(PanacheQuery.class);
        Quotation previous = new Quotation();
        previous.setCurrencyPrice(new BigDecimal("5.30"));

        CurrencyPriceDTO dto = new CurrencyPriceDTO(USDBRL.builder()
                .bid("5.20")
                .pctChange("-0.1")
                .build());
        when(client.getPriceByPair("USD-BRL")).thenReturn(dto);
        when(repository.findAll()).thenReturn(query);
        when(query.list()).thenReturn(List.of(previous));

        service.getCurrencyPrice();

        verify(kafkaEvents, never()).sendNewKafkaEvent(any());
        verify(repository, never()).persist(any(Quotation.class));
    }

    @Test
    void getCurrencyPricePersistsExpectedQuotationFields() {
        CurrencyPriceClient client = mock(CurrencyPriceClient.class);
        QuotationRepository repository = mock(QuotationRepository.class);
        KafkaEvents kafkaEvents = mock(KafkaEvents.class);
        QuotationServiceImpl service = new QuotationServiceImpl(client, repository, kafkaEvents);
        @SuppressWarnings("unchecked")
        PanacheQuery<Quotation> query = mock(PanacheQuery.class);

        CurrencyPriceDTO dto = new CurrencyPriceDTO(USDBRL.builder()
                .bid("5.15")
                .pctChange("1.1")
                .build());
        when(client.getPriceByPair("USD-BRL")).thenReturn(dto);
        when(repository.findAll()).thenReturn(query);
        when(query.list()).thenReturn(List.of());

        service.getCurrencyPrice();

        ArgumentCaptor<Quotation> quotationCaptor = ArgumentCaptor.forClass(Quotation.class);
        verify(repository).persist(quotationCaptor.capture());
        Quotation saved = quotationCaptor.getValue();
        assertEquals(new BigDecimal("5.15"), saved.getCurrencyPrice());
        assertEquals("1.1", saved.getPctChange());
        assertEquals("USD-BRL", saved.getPair());
        assertNotNull(saved.getDate());
    }
}

