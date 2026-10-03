package com.intellisure.quotepolicyservice.scheduler;

import com.intellisure.quotepolicyservice.service.PolicyService;
import com.intellisure.quotepolicyservice.service.QuoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class LifecycleScheduler {

    private final QuoteService quoteService;
    private final PolicyService policyService;

    @Scheduled(cron = "${intellisure.lifecycle.cron:0 0 0 * * *}")
    public void processLifecycleChanges() {
        Mono.zip(
                        quoteService.expireQuotedOffers(),
                        policyService.activateScheduledPolicies(),
                        policyService.expirePolicies()
                )
                .doOnNext(result ->
                        log.info(
                                "Lifecycle processing completed: "
                                        + "expired quotes={}, "
                                        + "activated policies={}, "
                                        + "expired policies={}",
                                result.getT1(),
                                result.getT2(),
                                result.getT3()
                        )
                )
                .doOnError(error ->
                        log.error(
                                "Lifecycle processing failed",
                                error
                        )
                )
                .subscribe();
    }
}