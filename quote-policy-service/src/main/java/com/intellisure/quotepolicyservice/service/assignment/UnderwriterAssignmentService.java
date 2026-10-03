package com.intellisure.quotepolicyservice.service.assignment;

import com.intellisure.quotepolicyservice.config.UnderwriterPoolProperties;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnderwriterAssignmentService {

    private final QuoteRepository quoteRepository;

    private final UnderwriterPoolProperties properties;

    public Mono<UUID> selectLeastLoadedUnderwriter() {
        List<UUID> eligibleUnderwriters =
                properties.getEligibleUnderwriterIds();

        if (eligibleUnderwriters == null
                || eligibleUnderwriters.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(eligibleUnderwriters)
                .flatMap(underwriterId ->
                        quoteRepository
                                .countActiveQuotesByUnderwriterId(
                                        underwriterId
                                )
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
                                        UnderwriterWorkload
                                                ::activeQuoteCount
                                )
                                .thenComparing(
                                        workload ->
                                                workload
                                                        .underwriterId()
                                                        .toString()
                                )
                )
                .next()
                .map(
                        UnderwriterWorkload::underwriterId
                );
    }
}