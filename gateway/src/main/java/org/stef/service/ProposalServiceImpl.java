package org.stef.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.stef.client.ProposalRestClient;
import org.stef.dto.ProposalDetailsDTO;

@ApplicationScoped
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRestClient proposalRestClient;

    @Inject
    public ProposalServiceImpl(@RestClient ProposalRestClient proposalRestClient) {
        this.proposalRestClient = proposalRestClient;
    }

    @Override
    public ProposalDetailsDTO getProposalDetailsById(Long id) {
        return proposalRestClient.getProposalDetailsById(id);
    }

    @Override
    public void createProposal(ProposalDetailsDTO proposalDetailsDTO) {
        proposalRestClient.createProposal(proposalDetailsDTO);
    }

    @Override
    public void removeProposal(Long id) {
        proposalRestClient.deleteProposal(id);
    }
}
