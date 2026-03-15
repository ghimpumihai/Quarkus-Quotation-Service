package org.stef.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stef.dto.OpportunityDTO;
import org.stef.dto.ProposalDTO;
import org.stef.dto.QuotationDTO;
import org.stef.entity.Opportunity;
import org.stef.entity.Quotation;
import org.stef.exception.QuotationPersistenceException;
import org.stef.exception.ReportGenerationException;
import org.stef.repository.OpportunitiesRepository;
import org.stef.repository.QuotationRepository;

import java.math.BigDecimal;
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
        try {
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
        } catch (PersistenceException e) {
            LOG.error("Failed to persist opportunity for proposal: {}", proposalDTO.proposalId(), e);
            throw new QuotationPersistenceException("Failed to persist opportunity", 500, e);
        } catch (Exception e) {
            LOG.error("Unexpected error building opportunity for proposal: {}", proposalDTO.proposalId(), e);
            throw new QuotationPersistenceException("Unexpected error building opportunity", 500, e);
        }
    }

    @Override
    @Transactional
    public void saveQuotation(QuotationDTO quotationDTO) {
        try {
            Quotation quotation = new Quotation();
            quotation.setCurrencyPrice(quotationDTO.currencyPrice());
            quotation.setDate(new Date());

            quotationRepository.persist(quotation);
        } catch (PersistenceException e) {
            LOG.error("Failed to persist quotation with price: {}", quotationDTO.currencyPrice(), e);
            throw new QuotationPersistenceException("Failed to persist quotation", 500, e);
        } catch (Exception e) {
            LOG.error("Unexpected error saving quotation with price: {}", quotationDTO.currencyPrice(), e);
            throw new QuotationPersistenceException("Unexpected error saving quotation", 500, e);
        }
    }


    public List<OpportunityDTO> generateOpportunityReport() {
        try {
            return opportunityRepository.findAll()
                    .stream()
                    .map(item -> OpportunityDTO.builder()
                            .proposalId(item.getProposalId())
                            .customer(item.getCustomer())
                            .priceTonne(item.getPriceTonne())
                            .lastCurrencyQuotation(item.getLastCurrencyQuotation())
                            .build())
                    .toList();
        } catch (Exception e) {
            LOG.error("Failed to generate opportunity report", e);
            throw new ReportGenerationException("Failed to generate opportunity report", e);
        }
    }
}
