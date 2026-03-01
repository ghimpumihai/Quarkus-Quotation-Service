package org.stef.service;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.stef.client.CurrencyPriceClient;
import org.stef.dto.CurrencyPriceDTO;
import org.stef.dto.QuotationDTO;
import org.stef.entity.QuotationEntity;
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
                    .currencyPrice(new BigDecimal(currencyPriceInfo.getUSDBRL().bid()))
                    .date(new Date())
                    .build());
        }
    }

    private boolean updateCurrentInfoPrice(CurrencyPriceDTO currencyPriceInfo){
        BigDecimal currentPrice = new BigDecimal(currencyPriceInfo.getUSDBRL().bid());
        boolean updatedPrice = false;
        List<QuotationEntity> quotationList = quotationRepository.findAll().list();
        if(quotationList.isEmpty()){
            saveQuotation(currencyPriceInfo);
            updatedPrice = true;
        }
        else{
            QuotationEntity lastDollarPrice = quotationList.getLast();

            if(currentPrice.floatValue() > lastDollarPrice.getCurrencyPrice().floatValue()){
                saveQuotation(currencyPriceInfo);
                updatedPrice = true;
            }
        }
        return updatedPrice;
    }

    private void saveQuotation(CurrencyPriceDTO currencyDTO){
        QuotationEntity quotation = new QuotationEntity();

        quotation.setDate(new Date());
        quotation.setCurrencyPrice(new BigDecimal(currencyDTO.getUSDBRL().bid()));
        quotation.setPctChange(currencyDTO.getUSDBRL().pctChange());
        quotation.setPair("USD-BRL");

        quotationRepository.persist(quotation);
    }
}
