package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.request.CreateQuoteCoverageRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRatingServiceTest {

    private final DefaultRatingService service = new DefaultRatingService();

    @Test
    void calculatesTransparentBaselineWithinConfiguredBounds() {
        CreateQuoteRequest request = request(
                "BOP",
                new CreateQuoteCoverageRequest(
                        "PROPERTY",
                        "Property",
                        new BigDecimal("100000"),
                        new BigDecimal("5000"),
                        30
                )
        );

        StepVerifier.create(service.calculateBasePremium(UUID.randomUUID(), "BOP", request))
                .assertNext(premium -> {
                    assertTrue(premium.compareTo(new BigDecimal("500.00")) >= 0);
                    assertTrue(premium.compareTo(new BigDecimal("1000000.00")) <= 0);
                    assertEquals(2, premium.scale());
                })
                .verifyComplete();
    }

    @Test
    void higherDeductibleProducesLowerBaselineForSameCoverage() {
        CreateQuoteCoverageRequest lowDeductible = new CreateQuoteCoverageRequest(
                "LIABILITY", "Liability", new BigDecimal("100000"), new BigDecimal("1000"), 0);
        CreateQuoteCoverageRequest highDeductible = new CreateQuoteCoverageRequest(
                "LIABILITY", "Liability", new BigDecimal("100000"), new BigDecimal("20000"), 0);

        BigDecimal low = service.calculateBasePremium(UUID.randomUUID(), "GENERAL_LIABILITY", request("GENERAL_LIABILITY", lowDeductible)).block();
        BigDecimal high = service.calculateBasePremium(UUID.randomUUID(), "GENERAL_LIABILITY", request("GENERAL_LIABILITY", highDeductible)).block();

        assertTrue(high.compareTo(low) < 0);
    }

    @Test
    void exposesProductSpecificRatingFactors() {
        StepVerifier.create(service.getRatingFactors("CYBER"))
                .assertNext(factors -> {
                    assertEquals(new BigDecimal("0.015625"), factors.baseRate());
                    assertEquals("COVERAGE_LIMIT_WITH_DEDUCTIBLE_AND_WAITING_PERIOD_ADJUSTMENTS", factors.rateBasis());
                    assertTrue(factors.applicableCoverages().contains("cyber"));
                })
                .verifyComplete();
    }

    @Test
    void coversValidationProductAndCoverageFactorBranches() {
        StepVerifier.create(service.calculateBasePremium(UUID.randomUUID(), null, null))
                .expectError(IllegalArgumentException.class).verify();
        CreateQuoteRequest nullCoverages = new CreateQuoteRequest(UUID.randomUUID(), "BOP", "need", "ops",
                LocalDate.now().plusDays(1), null);
        StepVerifier.create(service.calculateBasePremium(UUID.randomUUID(), "BOP", nullCoverages))
                .expectError(IllegalArgumentException.class).verify();
        CreateQuoteRequest emptyCoverages = new CreateQuoteRequest(UUID.randomUUID(), "BOP", "need", "ops",
                LocalDate.now().plusDays(1), List.of());
        StepVerifier.create(service.calculateBasePremium(UUID.randomUUID(), "BOP", emptyCoverages))
                .expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(service.calculateBasePremium(UUID.randomUUID(), "BOP",
                        request("BOP", new CreateQuoteCoverageRequest("CYBER", "Cyber",
                                new BigDecimal("100000000"), BigDecimal.ZERO, null))))
                .assertNext(premium -> assertEquals(new BigDecimal("1000000.00"), premium))
                .verifyComplete();

        String[] products = {"BUSINESS_OWNER_POLICY", "GENERAL_LIABILITY", "COMMERCIAL_PROPERTY",
                "CYBER", "PROFESSIONAL_LIABILITY", "unknown"};
        for (String product : products) {
            service.getRatingFactors(product).block();
        }
        String[] coverages = {"CYBER", "FIRE", "PROPERTY", "LIABILITY", "BUSINESS_INCOME", "PROFESSIONAL", "OTHER"};
        for (String coverage : coverages) {
            service.calculateBasePremium(UUID.randomUUID(), "BOP",
                    request("BOP", new CreateQuoteCoverageRequest(coverage, coverage,
                            new BigDecimal("100000"), new BigDecimal("500"), 365))).block();
        }
        service.calculateBasePremium(UUID.randomUUID(), "BOP",
                request("BOP", new CreateQuoteCoverageRequest(null, "unknown",
                        new BigDecimal("100000"), new BigDecimal("500"), null))).block();
        service.calculateOfferedTerms(UUID.randomUUID(), new com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest(null, List.of())).block();
        assertTrue(service.validateTerms("BOP", List.of(new com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest(
                "FIRE", BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null))).block());
        assertTrue(!service.validateTerms("BOP", List.of(new com.intellisure.quotepolicyservice.dto.request.OfferedCoverageRequest(
                "FIRE", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, null, null))).block());
    }

    private CreateQuoteRequest request(String productCode, CreateQuoteCoverageRequest coverage) {
        return new CreateQuoteRequest(
                UUID.randomUUID(),
                productCode,
                "Protect the business",
                "Small commercial operations",
                LocalDate.now().plusDays(10),
                List.of(coverage)
        );
    }
}
