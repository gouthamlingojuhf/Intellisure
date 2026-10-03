package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.request.CreateQuoteCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Default Rating Service Implementation
 * Placeholder implementation - replace with actual rating engine
 */
@Service
public class DefaultRatingService implements RatingService {

    @Override
    public Mono<BigDecimal> calculateBasePremium(
            UUID quoteId,
            String productCode,
            CreateQuoteRequest request) {
        // TODO: Implement actual rating logic
        // Current: 2% demo placeholder
        BigDecimal totalLimit = request.coverages().stream()
                .map(CreateQuoteCoverageRequest::requestedLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Mono.just(totalLimit.multiply(new BigDecimal("0.02")));
    }

    @Override
    public Mono<List<OfferedCoverageRequest>> calculateOfferedTerms(
            UUID quoteId,
            OfferQuoteTermsRequest request) {
        // Return the offered terms as-is (no recalculation)
        return Mono.just(request.coverages());
    }

    @Override
    public Mono<Boolean> validateTerms(
            String productCode,
            List<OfferedCoverageRequest> coverages) {
        // Basic validation
        boolean valid = coverages.stream()
                .allMatch(c -> c.offeredLimit().compareTo(BigDecimal.ZERO) > 0
                        && c.offeredDeductible().compareTo(BigDecimal.ZERO) >= 0
                        && c.coveragePremium().compareTo(BigDecimal.ZERO) >= 0);
        return Mono.just(valid);
    }

    @Override
    public Mono<RatingFactors> getRatingFactors(String productCode) {
        return Mono.just(new RatingFactors(
                new BigDecimal("0.02"), // 2% base rate
                "TOTAL_LIMIT",
                List.of("fire", "liability", "property", "business_interruption"),
                new BigDecimal("500.00"),
                new BigDecimal("1000000.00")
        ));
    }
}