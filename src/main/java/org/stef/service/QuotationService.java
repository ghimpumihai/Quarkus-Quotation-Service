package org.stef.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.stef.client.CurrencyPriceClient;
import org.stef.dto.CurrencyPriceDTO;
import org.stef.dto.QuotationDTO;
import org.stef.entity.Quotation;
import org.stef.message.KafkaEvents;
import org.stef.repository.QuotationRepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class QuotationService {

    @Inject
    @RestClient
    CurrencyPriceClient currencyPriceClient;

    @Inject
    QuotationRepository quotationRepository;

    @Inject
    KafkaEvents kafkaEvents;

    public void getCurrencyPrice(){
        CurrencyPriceDTO currencyPriceInfo = currencyPriceClient.getPriceByPair("USD-BRL");
        if(updateCurrentInfoPrice(currencyPriceInfo)){
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
        if(quotationList.isEmpty()){
            saveQuotation(currencyPriceInfo);
            updatedPrice = true;
        }
        else{
            Quotation lastDollarPrice = quotationList.getLast();

            if(currentPrice.floatValue() > lastDollarPrice.getCurrencyPrice().floatValue()){
                saveQuotation(currencyPriceInfo);
                updatedPrice = true;
            }
        }
        return updatedPrice;
    }

    private void saveQuotation(CurrencyPriceDTO currencyDTO){
        Quotation quotation = new Quotation();

        quotation.setDate(new Date());
        quotation.setCurrencyPrice(new BigDecimal(currencyDTO.USDBRL().bid()));
        quotation.setPctChange(currencyDTO.USDBRL().pctChange());
        quotation.setPair("USD-BRL");

        quotationRepository.persist(quotation);
    }
}
