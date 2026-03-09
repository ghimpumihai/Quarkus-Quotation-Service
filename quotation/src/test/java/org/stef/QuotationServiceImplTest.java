package org.stef;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.junit.jupiter.api.Test;
import org.stef.client.CurrencyPriceClient;
import org.stef.dto.CurrencyPriceDTO;
import org.stef.dto.USDBRL;
import org.stef.entity.Quotation;
import org.stef.message.KafkaEvents;
import org.stef.repository.QuotationRepository;
import org.stef.service.QuotationServiceImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
}
