package org.stef.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stef.service.QuotationService;

@ApplicationScoped
public class QuotationScheduler {

    @Inject
    QuotationService quotationService;

    private final Logger LOG = LoggerFactory.getLogger(QuotationScheduler.class);

    @Transactional
    @Scheduled(every = "60s", identity = "task-job")
    void schedule(){
        LOG.info("Scheduled task started: Fetching currency price...");
        quotationService.getCurrencyPrice();
    }
}
