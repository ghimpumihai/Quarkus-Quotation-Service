package org.stef.message;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import jakarta.enterprise.context.ApplicationScoped;
import org.stef.dto.QuotationDTO;

@ApplicationScoped
public class KafkaEvents {

    private final Logger LOG= LoggerFactory.getLogger(KafkaEvents.class);

    @Channel("quotation-channel")
    Emitter<QuotationDTO> quotationRequestEmitter;

    public void sendNewKafkaEvent(QuotationDTO quotationDTO){
        LOG.info("Sending new Kafka event");
        quotationRequestEmitter.send(quotationDTO).toCompletableFuture().join();
    }
}
