package org.stef.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stef.exception.InvalidCurrencyCodeException;
import org.stef.exception.ProviderUnavailableException;
import org.stef.service.QuotationService;

@ApplicationScoped
public class QuotationScheduler {

    private final QuotationService quotationService;

    private final Logger LOG = LoggerFactory.getLogger(QuotationScheduler.class);

    @Inject
    public QuotationScheduler(QuotationService quotationService) {
        this.quotationService = quotationService;
    }

    @Transactional
    @Scheduled(every = "60s", identity = "task-job")
    void schedule() {
        LOG.info("Scheduled task started: Fetching currency price...");
        try {
            quotationService.getCurrencyPrice();
        } catch (ProviderUnavailableException e) {
            LOG.warn("Quotation fetch failed: {}", e.getMessage());
        } catch(InvalidCurrencyCodeException e) {
            LOG.error("Invalid currency code during scheduled fetch: {}", e.getMessage());
        } catch (Exception e) {
            LOG.error("Unexpected error during scheduled fetch", e);
        }
    }
}
