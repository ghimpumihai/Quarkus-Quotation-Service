package org.stef.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stef.client.CurrencyPriceClient;
import org.stef.dto.CurrencyPriceDTO;
import org.stef.dto.QuotationDTO;
import org.stef.entity.Quotation;
import org.stef.exception.InvalidCurrencyCodeException;
import org.stef.exception.ProviderUnavailableException;
import org.stef.message.KafkaEvents;
import org.stef.repository.QuotationRepository;


import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class QuotationServiceImpl implements QuotationService {

    private final CurrencyPriceClient currencyPriceClient;

    private final QuotationRepository quotationRepository;

    private final KafkaEvents kafkaEvents;

    private final Logger LOG = LoggerFactory.getLogger(QuotationServiceImpl.class);

    @Inject
    public QuotationServiceImpl(@RestClient CurrencyPriceClient currencyPriceClient,
                                QuotationRepository quotationRepository,
                                KafkaEvents kafkaEvents) {
        this.currencyPriceClient = currencyPriceClient;
        this.quotationRepository = quotationRepository;
        this.kafkaEvents = kafkaEvents;
    }


    @Override
    public void getCurrencyPrice() {
        CurrencyPriceDTO currencyPriceInfo;

        try {
            currencyPriceInfo = currencyPriceClient.getPriceByPair("USD-BRL");
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status >= 500) {
                throw new ProviderUnavailableException(
                        "Currency API is unavailable (HTTP " + status + ")", e
                );
            }
            if (status == 400) {
                throw new InvalidCurrencyCodeException("USD-BRL");
            }
            throw new ProviderUnavailableException(
                    "Unexpected response from currency API (HTTP " + status + ")", e
            );
        }

        if (updateCurrentInfoPrice(currencyPriceInfo)) {
            kafkaEvents.sendNewKafkaEvent(QuotationDTO
                    .builder()
                    .currencyPrice(new BigDecimal(currencyPriceInfo.USDBRL().bid()))
                    .date(new Date())
                    .build());
        }
    }

    private boolean updateCurrentInfoPrice(CurrencyPriceDTO currencyPriceInfo){
        BigDecimal currentPrice = new BigDecimal(currencyPriceInfo.USDBRL().bid());
        boolean updatedPrice = false;
        List<Quotation> quotationList = quotationRepository.findAll().list();
        LOG.info("Comparing current price with the last recorded price...");
        if(quotationList.isEmpty()){
            LOG.info("No previous quotations found. Saving the first quotation.");
            saveQuotation(currencyPriceInfo);
            updatedPrice = true;
        }
        else{
            LOG.info("Last recorded price: USD-BRL = {}", quotationList.getLast().getCurrencyPrice());
            Quotation lastDollarPrice = quotationList.getLast();

            if(currentPrice.floatValue() > lastDollarPrice.getCurrencyPrice().floatValue()){
                saveQuotation(currencyPriceInfo);
                updatedPrice = true;
            }
        }
        return updatedPrice;
    }

    private void saveQuotation(CurrencyPriceDTO currencyDTO){
        LOG.info("Saving new quotation: USD-BRL = {}", currencyDTO.USDBRL().bid());
        Quotation quotation = new Quotation();

        quotation.setDate(new Date());
        quotation.setCurrencyPrice(new BigDecimal(currencyDTO.USDBRL().bid()));
        quotation.setPctChange(currencyDTO.USDBRL().pctChange());
        quotation.setPair("USD-BRL");

        quotationRepository.persist(quotation);
    }
}

