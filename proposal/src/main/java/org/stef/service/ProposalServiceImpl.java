package org.stef.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.stef.dto.ProposalDTO;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.entity.Proposal;
import org.stef.exception.ProposalCreationException;
import org.stef.exception.ProposalNotFoundException;
import org.stef.message.KafkaEvent;
import org.stef.repository.ProposalRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ProposalServiceImpl implements ProposalService{

    private final ProposalRepository proposalRepository;

    private final KafkaEvent kafkaMessages;

    private static final Logger LOG = Logger.getLogger(ProposalServiceImpl.class);

    @Inject
    public ProposalServiceImpl(ProposalRepository proposalRepository, KafkaEvent kafkaMessages) {
        this.proposalRepository = proposalRepository;
        this.kafkaMessages = kafkaMessages;
    }

    @Override
    public ProposalDetailsDTO findFullProposal(Long id) {
        Proposal proposal = proposalRepository.findById(id);

        if (proposal == null) {
            throw new ProposalNotFoundException(id);
        }

        return ProposalDetailsDTO.builder()
                .proposalId(proposal.getId())
                .customer(proposal.getCustomer())
                .priceTonne(proposal.getPriceTonne())
                .tonnes(proposal.getTonnes())
                .country(proposal.getCountry())
                .proposalValidityDays(proposal.getProposalValidityDays())
                .build();
    }

    @Transactional
    public void createProposal(ProposalDetailsDTO proposalDetailsDTO) {
        try {
            Proposal proposal = buildProposal(proposalDetailsDTO);
            proposalRepository.persist(proposal);
            kafkaMessages.sendProposalRequest(toDTO(proposal));
        } catch (PersistenceException e) {
            LOG.errorf(e, "Failed to persist proposal for customer %s", proposalDetailsDTO.customer());
            throw new ProposalCreationException("Failed to persist proposal", e);
        } catch (Exception e) {
            LOG.error("Unexpected error creating proposal", e);
            throw new ProposalCreationException("Unexpected error creating proposal", e);
        }
    }
    private Proposal buildProposal(ProposalDetailsDTO dto) {
        return Proposal.builder()
                .customer(dto.customer())
                .priceTonne(dto.priceTonne())
                .tonnes(dto.tonnes())
                .country(dto.country())
                .proposalValidityDays(dto.proposalValidityDays())
                .build();
    }

    private ProposalDTO toDTO(Proposal proposal) {
        return ProposalDTO.builder()
                .proposalId(proposal.getId())
                .customer(proposal.getCustomer())
                .priceTonne(proposal.getPriceTonne())
                .build();
    }

    @Override
    @Transactional
    public void removeProposal(Long id) {
        proposalRepository.deleteById(id);
    }
}
