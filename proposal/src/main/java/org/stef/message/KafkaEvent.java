package org.stef.message;


import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stef.dto.ProposalDTO;


@ApplicationScoped
public class KafkaEvent {

    private final Logger LOG= LoggerFactory.getLogger(KafkaEvent.class);

    @Channel("proposal-channel")
    Emitter<ProposalDTO> proposalRequestEmitter;

    public void sendProposalRequest(ProposalDTO proposalDTO) {
        LOG.info("Sending proposal request to Kafka: {}", proposalDTO);
        proposalRequestEmitter.send(proposalDTO).toCompletableFuture().join();
    }
}
