package org.stef.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
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
    public Response createProposal(ProposalDetailsDTO proposalDetailsDTO) {
        return proposalRestClient.createProposal(proposalDetailsDTO);
    }

    @Override
    public Response removeProposal(Long id) {
        return proposalRestClient.deleteProposal(id);
    }
}
