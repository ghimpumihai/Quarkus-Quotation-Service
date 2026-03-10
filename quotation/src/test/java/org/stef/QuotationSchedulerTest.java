package org.stef;

import org.junit.jupiter.api.Test;
import org.stef.scheduler.QuotationScheduler;
import org.stef.service.QuotationService;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QuotationSchedulerTest {

    @Test
    void scheduleInvokesQuotationService() throws Exception {
        QuotationService quotationService = mock(QuotationService.class);
        QuotationScheduler scheduler = new QuotationScheduler(quotationService);

        Method schedule = QuotationScheduler.class.getDeclaredMethod("schedule");
        schedule.setAccessible(true);
        schedule.invoke(scheduler);

        verify(quotationService).getCurrencyPrice();
    }
}

