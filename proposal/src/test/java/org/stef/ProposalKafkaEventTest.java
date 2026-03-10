package org.stef;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.Test;
import org.stef.dto.ProposalDTO;
import org.stef.message.KafkaEvent;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProposalKafkaEventTest {

    @Test
    void sendProposalRequestUsesEmitterAndWaitsForCompletion() throws Exception {
        KafkaEvent kafkaEvent = new KafkaEvent();
        @SuppressWarnings("unchecked")
        Emitter<ProposalDTO> emitter = mock(Emitter.class);
        @SuppressWarnings("unchecked")
        CompletionStage<Void> completionStage = mock(CompletionStage.class);
        when(completionStage.toCompletableFuture()).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));
        when(emitter.send(any(ProposalDTO.class))).thenReturn(completionStage);
        injectEmitter(kafkaEvent, emitter);

        ProposalDTO dto = ProposalDTO.builder()
                .proposalId(10L)
                .customer("ACME")
                .priceTonne(new BigDecimal("45.00"))
                .build();

        kafkaEvent.sendProposalRequest(dto);

        verify(emitter).send(dto);
    }

    private void injectEmitter(KafkaEvent kafkaEvent, Emitter<ProposalDTO> emitter) throws Exception {
        Field field = KafkaEvent.class.getDeclaredField("proposalRequestEmitter");
        field.setAccessible(true);
        field.set(kafkaEvent, emitter);
    }
}
