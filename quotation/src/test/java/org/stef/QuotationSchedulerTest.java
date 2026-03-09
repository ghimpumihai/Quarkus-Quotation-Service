package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.scheduler.QuotationScheduler;
import org.stef.service.QuotationService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class QuotationSchedulerTest {

    @Test
    void schedulerCanBeConstructedWithServiceDependency() {
        QuotationService quotationService = mock(QuotationService.class);
        QuotationScheduler scheduler = new QuotationScheduler(quotationService);

        assertNotNull(scheduler);
    }
}
