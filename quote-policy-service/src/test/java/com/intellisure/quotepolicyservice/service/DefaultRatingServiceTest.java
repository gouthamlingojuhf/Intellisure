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
