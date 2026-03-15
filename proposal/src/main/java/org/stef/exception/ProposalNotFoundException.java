package org.stef.exception;

public class ProposalNotFoundException extends ProposalException {

    public ProposalNotFoundException(Long id) {
        super("Proposal not found with id: " + id, 404);
    }
}