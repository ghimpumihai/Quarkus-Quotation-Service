package org.stef.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;
import org.stef.entity.Opportunity;
import org.stef.entity.Quotation;
import org.stef.repository.OpportunitiesRepository;
import org.stef.repository.QuotationRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class OpportunityServiceImpl implements OpportunityService {

    private final QuotationRepository quotationRepository;

    private final OpportunitiesRepository opportunityRepository;

    private static final Logger LOG = LoggerFactory.getLogger(OpportunityServiceImpl.class);

    @Inject
    public OpportunityServiceImpl(QuotationRepository quotationRepository, OpportunitiesRepository opportunityRepository) {
        this.quotationRepository = quotationRepository;
        this.opportunityRepository = opportunityRepository;
    }

    @Override
    @Transactional
    public void buildOpportunity(ProposalDTO proposalDTO) {
        List<Quotation> quotations = quotationRepository.findAll().list();

        Opportunity opportunity = new Opportunity();
        opportunity.setCustomer(proposalDTO.customer());
        opportunity.setDate(new Date());
        opportunity.setPriceTonne(proposalDTO.priceTonne());
        opportunity.setProposalId(proposalDTO.proposalId());

        if (!quotations.isEmpty()) {
            opportunity.setLastCurrencyQuotation(quotations.getLast().getCurrencyPrice());
        } else {
            LOG.warn("No quotations found in DB for proposal: {}", proposalDTO.proposalId());
            opportunity.setLastCurrencyQuotation(BigDecimal.valueOf(0.0));
        }

        opportunityRepository.persist(opportunity);
    }

    @Override
    @Transactional
    public void saveQuotation(QuotationDTO quotationDTO) {
        Quotation quotation = new Quotation();
        quotation.setCurrencyPrice(quotationDTO.currencyPrice());
        quotation.setDate(new Date());

        quotationRepository.persist(quotation);
    }

    public List<OpportunityDTO> generateOpportunityReport(){
        List<OpportunityDTO> opportunities= new ArrayList<>();

        opportunityRepository.findAll()
                .stream()
                .forEach(item->
                        opportunities.add(OpportunityDTO.builder()
                        .proposalId(item.getProposalId())
                        .customer(item.getCustomer())
                        .priceTonne(item.getPriceTonne())
                        .lastCurrencyQuotation(item.getLastCurrencyQuotation())
                        .build()
                ));
        return opportunities;
    }
}
