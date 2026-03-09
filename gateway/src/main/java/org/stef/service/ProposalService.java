package org.stef.service;


import jakarta.ws.rs.core.Response;
import org.stef.dto.ProposalDetailsDTO;

public interface ProposalService {

    ProposalDetailsDTO getProposalDetailsById(Long id);

    Response createProposal(ProposalDetailsDTO proposalDetailsDTO);

    Response removeProposal(Long id);
}
