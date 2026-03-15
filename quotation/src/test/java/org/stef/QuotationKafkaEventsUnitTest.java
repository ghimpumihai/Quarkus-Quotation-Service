package org.stef;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Test;
import org.stef.dto.QuotationDTO;
import org.stef.message.KafkaEvents;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuotationKafkaEventsUnitTest {

    @Test
    void sendNewKafkaEventUsesEmitter() throws Exception {
        KafkaEvents kafkaEvents = new KafkaEvents();
        @SuppressWarnings("unchecked")
        Emitter<QuotationDTO> emitter = mock(Emitter.class);
        @SuppressWarnings("unchecked")
        CompletionStage<Void> completionStage = mock(CompletionStage.class);
        when(completionStage.toCompletableFuture()).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
        when(emitter.send(any(QuotationDTO.class))).thenReturn(completionStage);
        injectEmitter(kafkaEvents, emitter);

        QuotationDTO dto = QuotationDTO.builder()
                .date(new Date())
                .currencyPrice(new BigDecimal("5.40"))
                .build();

        kafkaEvents.sendNewKafkaEvent(dto);

        verify(emitter).send(dto);
    }

    private void injectEmitter(KafkaEvents kafkaEvents, Emitter<QuotationDTO> emitter) throws Exception {
        Field field = KafkaEvents.class.getDeclaredField("quotationRequestEmitter");
        field.setAccessible(true);
        field.set(kafkaEvents, emitter);
    }
}
