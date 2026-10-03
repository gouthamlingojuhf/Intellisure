package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Rating Service Interface
 * Implementations should provide product-specific premium calculation logic
 * to replace the 2% demo placeholder.
 */
public interface RatingService {

    /**
     * Calculate base premium for a draft quote
     */
    Mono<BigDecimal> calculateBasePremium(
            UUID quoteId,
            String productCode,
            CreateQuoteRequest request);

    /**
     * Calculate coverage premiums for offered terms
     */
    Mono<List<OfferedCoverageRequest>> calculateOfferedTerms(
            UUID quoteId,
            OfferQuoteTermsRequest request);

    /**
     * Validate that coverage terms are within acceptable ranges
     */
    Mono<Boolean> validateTerms(
            String productCode,
            List<OfferedCoverageRequest> coverages);

    /**
     * Get rating factors for a product
     */
    Mono<RatingFactors> getRatingFactors(String productCode);

    record RatingFactors(
            BigDecimal baseRate,
            String rateBasis,
            List<String> applicableCoverages,
            BigDecimal minPremium,
            BigDecimal maxPremium
    ) {}
}