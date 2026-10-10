package com.intellisure.quotepolicyservice.service.assignment;

import com.intellisure.quotepolicyservice.client.CustomerPartyClient;
import com.intellisure.quotepolicyservice.config.UnderwriterPoolProperties;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnderwriterAssignmentService {

    private final QuoteRepository quoteRepository;
    private final CustomerPartyClient customerPartyClient;
    private final UnderwriterPoolProperties properties;

    public Mono<UUID> selectLeastLoadedUnderwriter() {
        return customerPartyClient.findAvailableEmployeesByRole("UNDERWRITER")
                .collectList()
                .flatMap(fromDb -> {
                    List<UUID> eligible = (fromDb != null && !fromDb.isEmpty())
                            ? fromDb
                            : properties.getEligibleUnderwriterIds();

                    if (eligible == null || eligible.isEmpty()) {
                        log.info("No eligible underwriters found in database or configuration for auto-assignment");
                        return Mono.empty();
                    }

                    return Flux.fromIterable(eligible)
                            .flatMap(underwriterId ->
                                    quoteRepository
                                            .countActiveQuotesByUnderwriterId(underwriterId)
                                            .defaultIfEmpty(0L)
                                            .map(activeCount ->
                                                    new UnderwriterWorkload(
                                                            underwriterId,
                                                            activeCount
                                                    )
                                            )
                            )
                            .sort(
                                    Comparator
                                            .comparingLong(
                                                    UnderwriterWorkload::activeQuoteCount
                                            )
                                            .thenComparing(
                                                    workload ->
                                                            workload
                                                                    .underwriterId()
                                                                    .toString()
                                            )
                            )
                            .next()
                            .map(UnderwriterWorkload::underwriterId);
                });
    }
}