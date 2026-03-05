package org.stef.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.stef.dto.ProposalDTO;
import org.stef.dto.ProposalDetailsDTO;
import org.stef.entity.Proposal;
import org.stef.message.KafkaEvent;
import org.stef.repository.ProposalRepository;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ProposalServiceImpl implements ProposalService{

    @Inject
    ProposalRepository proposalRepository;

    @Inject
    KafkaEvent kafkaMessages;

    private static final Logger LOG = Logger.getLogger(ProposalServiceImpl.class);

    @Override
    public ProposalDetailsDTO findFullProposal(Long id) {
        Proposal proposal = proposalRepository.findById(id);

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
            throw new RuntimeException("Failed to persist proposal: " + e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error creating proposal", e);
            throw new RuntimeException("Unexpected error creating proposal: " + e.getMessage());
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
