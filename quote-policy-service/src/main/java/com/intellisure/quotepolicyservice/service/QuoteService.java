package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.QuoteResponse;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.QuoteMapper;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    private final QuoteMapper quoteMapper;


    public Mono<QuoteResponse> createQuote(
            CreateQuoteRequest request) {

        LocalDateTime now =
                LocalDateTime.now();

        Quote quote =
                Quote.builder()

                        .quoteId(UUID.randomUUID())

                        .customerId(request.customerId())

                        .businessName(
                                request.businessName()
                        )

                        .businessType(
                                request.businessType()
                        )

                        .annualRevenue(
                                request.annualRevenue()
                        )

                        .employeeCount(
                                request.employeeCount()
                        )

                        .requestedCoverageAmount(
                                request.requestedCoverageAmount()
                        )

                        /*
                         * Temporary initial estimate.
                         *
                         * Real risk-based pricing will happen
                         * after Risk & Underwriting Service
                         * is implemented.
                         */
                        .estimatedPremium(
                                calculateInitialPremium(
                                        request.requestedCoverageAmount()
                                )
                        )

                        .quoteStatus("DRAFT")

                        .validUntil(
                                now.plusDays(30)
                        )

                        .createdAt(now)

                        .updatedAt(now)

                        .build();
                        System.out.println(quote);
        return r2dbcEntityTemplate
                .insert(quote)
                .map(quoteMapper::toQuoteResponse);
    }


    public Mono<QuoteResponse> getQuote(
            UUID quoteId) {

        return quoteRepository
                .findById(quoteId)

                .map(quoteMapper::toQuoteResponse)

                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException(
                                        "Quote not found: "
                                                + quoteId
                                )
                        )
                );
    }


    public Flux<QuoteResponse> getQuotesByCustomer(
            UUID customerId) {

        return quoteRepository
                .findByCustomerId(customerId)

                .map(quoteMapper::toQuoteResponse);
    }

    public Mono<QuoteResponse> updateStatus(UUID quoteId, String status) {
        return quoteRepository.findById(quoteId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Quote not found: " + quoteId)))
                .flatMap(quote -> {
                    quote.setQuoteStatus(status);
                    quote.setUpdatedAt(LocalDateTime.now());
                    quote.setNew(false);
                    return quoteRepository.save(quote);
                }).map(quoteMapper::toQuoteResponse);
    }


    private BigDecimal calculateInitialPremium(
            BigDecimal coverageAmount) {

        /*
         * Temporary demo calculation:
         * 2% of requested coverage.
         *
         * This will later be replaced by
         * Risk & Underwriting driven pricing.
         */
        return coverageAmount
                .multiply(
                        new BigDecimal("0.02")
                );
    }
}
