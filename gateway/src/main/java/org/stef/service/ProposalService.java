package org.stef.service;

import org.stef.dto.ProposalDetailsDTO;

public interface ProposalService {
    ProposalDetailsDTO getProposalDetailsById(Long id);
    void createProposal(ProposalDetailsDTO proposalDetailsDTO);
    void removeProposal(Long id);
}
