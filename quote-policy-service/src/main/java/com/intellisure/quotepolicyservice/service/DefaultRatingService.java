package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.request.CreateQuoteCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Default Rating Service Implementation
 * Transparent baseline indication used by the quote workflow before
 * an underwriter applies final commercial terms.
 */
@Service
public class DefaultRatingService implements RatingService {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.0125");
    private static final BigDecimal MIN_PREMIUM = new BigDecimal("500.00");
    private static final BigDecimal MAX_PREMIUM = new BigDecimal("1000000.00");

    @Override
    public Mono<BigDecimal> calculateBasePremium(
            UUID quoteId,
            String productCode,
            CreateQuoteRequest request) {
        if (request == null || request.coverages() == null || request.coverages().isEmpty()) {
            return Mono.error(new IllegalArgumentException("At least one coverage is required for rating"));
        }

        BigDecimal productFactor = productFactor(productCode);
        BigDecimal premium = request.coverages().stream()
                .map(coverage -> coveragePremium(coverage, productFactor))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Mono.just(clamp(premium));
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
                BASE_RATE.multiply(productFactor(productCode)).setScale(6, RoundingMode.HALF_UP),
                "COVERAGE_LIMIT_WITH_DEDUCTIBLE_AND_WAITING_PERIOD_ADJUSTMENTS",
                List.of("fire", "liability", "property", "business_interruption", "cyber", "professional_liability"),
                MIN_PREMIUM,
                MAX_PREMIUM
        ));
    }

    private BigDecimal coveragePremium(
            CreateQuoteCoverageRequest coverage,
            BigDecimal productFactor
    ) {
        BigDecimal limit = coverage.requestedLimit();
        BigDecimal deductibleRatio = coverage.requestedDeductible()
                .divide(limit, 8, RoundingMode.HALF_UP)
                .min(new BigDecimal("0.70"));
        BigDecimal deductibleFactor = BigDecimal.ONE
                .subtract(deductibleRatio.multiply(new BigDecimal("0.35")));
        BigDecimal waitingPeriod = BigDecimal.valueOf(
                coverage.waitingPeriodDays() == null ? 0 : coverage.waitingPeriodDays()
        );
        BigDecimal waitingFactor = BigDecimal.ONE.add(
                waitingPeriod.divide(new BigDecimal("365"), 8, RoundingMode.HALF_UP)
                        .min(new BigDecimal("0.15"))
                        .multiply(new BigDecimal("0.10"))
        );

        return limit
                .multiply(BASE_RATE)
                .multiply(productFactor)
                .multiply(coverageFactor(coverage.coverageCode()))
                .multiply(deductibleFactor)
                .multiply(waitingFactor);
    }

    private BigDecimal productFactor(String productCode) {
        String normalized = productCode == null
                ? ""
                : productCode.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "BOP", "BUSINESS_OWNER_POLICY" -> new BigDecimal("1.00");
            case "GENERAL_LIABILITY" -> new BigDecimal("0.90");
            case "COMMERCIAL_PROPERTY" -> new BigDecimal("1.10");
            case "CYBER" -> new BigDecimal("1.25");
            case "PROFESSIONAL_LIABILITY" -> new BigDecimal("1.05");
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal coverageFactor(String coverageCode) {
        String normalized = coverageCode == null
                ? ""
                : coverageCode.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("cyber")) return new BigDecimal("1.25");
        if (normalized.contains("fire")) return new BigDecimal("1.15");
        if (normalized.contains("property")) return new BigDecimal("1.10");
        if (normalized.contains("liability")) return new BigDecimal("0.90");
        if (normalized.contains("business") || normalized.contains("income")) {
            return new BigDecimal("0.95");
        }
        if (normalized.contains("professional")) return new BigDecimal("1.05");
        return BigDecimal.ONE;
    }

    private BigDecimal clamp(BigDecimal premium) {
        return premium.max(MIN_PREMIUM).min(MAX_PREMIUM).setScale(2, RoundingMode.HALF_UP);
    }
}
