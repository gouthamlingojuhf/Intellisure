package com.intellisure.quotepolicyservice.scheduler;

import com.intellisure.quotepolicyservice.service.PolicyService;
import com.intellisure.quotepolicyservice.service.QuoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("LifecycleScheduler")
class LifecycleSchedulerTest {

    private QuoteService quoteService;
    private PolicyService policyService;

    private LifecycleScheduler scheduler;

    @BeforeEach
    void setUp() {
        quoteService = mock(QuoteService.class);
        policyService = mock(PolicyService.class);

        scheduler = new LifecycleScheduler(
                quoteService,
                policyService
        );
    }

    @Test
    @DisplayName("triggers every lifecycle transition")
    void triggersAllTransitions() {
        when(quoteService.expireQuotedOffers()).thenReturn(Mono.just(1L));
        when(
                policyService.activateScheduledPolicies()
        ).thenReturn(Mono.just(2L));
        when(policyService.expirePolicies()).thenReturn(Mono.just(3L));

        scheduler.processLifecycleChanges();

        verify(quoteService).expireQuotedOffers();
        verify(policyService).activateScheduledPolicies();
        verify(policyService).expirePolicies();
        verifyNoMoreInteractions(quoteService, policyService);
    }

    @Test
    @DisplayName("swallows lifecycle failures instead of propagating them")
    void swallowsFailures() {
        when(quoteService.expireQuotedOffers()).thenReturn(
                Mono.error(new IllegalStateException("db down"))
        );
        when(
                policyService.activateScheduledPolicies()
        ).thenReturn(Mono.just(0L));
        when(policyService.expirePolicies()).thenReturn(Mono.just(0L));

        scheduler.processLifecycleChanges();

        verify(quoteService).expireQuotedOffers();
    }

    @Test
    @DisplayName("handles an empty lifecycle run")
    void handlesEmptyRun() {
        when(quoteService.expireQuotedOffers()).thenReturn(Mono.just(0L));
        when(
                policyService.activateScheduledPolicies()
        ).thenReturn(Mono.just(0L));
        when(policyService.expirePolicies()).thenReturn(Mono.just(0L));

        scheduler.processLifecycleChanges();

        verify(policyService).expirePolicies();
    }
}
