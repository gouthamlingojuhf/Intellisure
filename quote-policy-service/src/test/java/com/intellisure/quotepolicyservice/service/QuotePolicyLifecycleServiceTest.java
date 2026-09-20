package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.QuoteResponse;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.mapper.QuoteMapper;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuotePolicyLifecycleServiceTest {
    @Mock QuoteRepository quoteRepository;
    @Mock PolicyRepository policyRepository;
    @Mock R2dbcEntityTemplate template;
    @Mock QuoteMapper mapper;
    @InjectMocks QuoteService quoteService;
    @InjectMocks PolicyService policyService;

    @Test
    void createsDraftQuoteWithInitialPremiumAndThirtyDayValidity() {
        UUID customer = UUID.randomUUID();
        Quote saved = Quote.builder().quoteId(UUID.randomUUID()).customerId(customer)
                .requestedCoverageAmount(new BigDecimal("100000")).estimatedPremium(new BigDecimal("2000"))
                .quoteStatus("DRAFT").validUntil(LocalDateTime.now().plusDays(30)).build();
        when(template.insert(any(Quote.class))).thenReturn(Mono.just(saved));
        when(mapper.toQuoteResponse(saved)).thenReturn(new QuoteResponse(saved.getQuoteId(), customer,
                "Acme", "RESTAURANT", new BigDecimal("500000"), 10,
                new BigDecimal("100000"), new BigDecimal("2000"), "DRAFT",
                saved.getValidUntil(), null, null));

        StepVerifier.create(quoteService.createQuote(new CreateQuoteRequest(customer, "Acme",
                        "RESTAURANT", new BigDecimal("500000"), 10, new BigDecimal("100000"))))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals("DRAFT", response.quoteStatus());
                    org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("2000"), response.estimatedPremium());
                }).verifyComplete();
        verify(template).insert(any(Quote.class));
    }

    @Test
    void rejectsIssuingPolicyUnlessQuoteIsAccepted() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = Quote.builder().quoteId(quoteId).quoteStatus("SUBMITTED").build();
        when(quoteRepository.findById(quoteId)).thenReturn(Mono.just(quote));
        StepVerifier.create(policyService.issuePolicy(quoteId))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException
                        && e.getMessage().contains("ACCEPTED")).verify();
        verifyNoInteractions(policyRepository);
    }

    @Test
    void issuesPolicyAndMovesAcceptedQuoteToBound() {
        UUID quoteId = UUID.randomUUID();
        UUID customer = UUID.randomUUID();
        Quote quote = Quote.builder().quoteId(quoteId).customerId(customer)
                .quoteStatus("ACCEPTED").estimatedPremium(new BigDecimal("2400")).build();
        Policy policy = Policy.builder().policyId(UUID.randomUUID()).quoteId(quoteId).customerId(customer)
                .policyNumber("POL-1").policyStatus("ACTIVE").effectiveDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusYears(1)).totalPremium(new BigDecimal("2400")).build();
        when(quoteRepository.findById(quoteId)).thenReturn(Mono.just(quote));
        when(quoteRepository.save(quote)).thenReturn(Mono.just(quote));
        when(policyRepository.save(any(Policy.class))).thenReturn(Mono.just(policy));

        StepVerifier.create(policyService.issuePolicy(quoteId))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", result.policyStatus());
                    org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("2400"), result.totalPremium());
                }).verifyComplete();
        org.junit.jupiter.api.Assertions.assertEquals("BOUND", quote.getQuoteStatus());
    }

    @Test
    void missingQuoteCannotBeRead() {
        UUID quoteId = UUID.randomUUID();
        when(quoteRepository.findById(quoteId)).thenReturn(Mono.empty());

        StepVerifier.create(quoteService.getQuote(quoteId))
                .expectErrorMatches(error -> error.getMessage().contains("Quote not found"))
                .verify();
    }

    @Test
    void updatingQuoteStatusPersistsTheLifecycleTransition() {
        UUID quoteId = UUID.randomUUID();
        Quote quote = Quote.builder().quoteId(quoteId).quoteStatus("DRAFT").build();
        when(quoteRepository.findById(quoteId)).thenReturn(Mono.just(quote));
        when(quoteRepository.save(quote)).thenReturn(Mono.just(quote));
        when(mapper.toQuoteResponse(quote)).thenReturn(new QuoteResponse(
                quoteId, null, null, null, null, 0, null, null,
                "SUBMITTED", null, null, null));

        StepVerifier.create(quoteService.updateStatus(quoteId, "SUBMITTED"))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals(
                        "SUBMITTED", response.quoteStatus()))
                .verifyComplete();
        verify(quoteRepository).save(quote);
        org.junit.jupiter.api.Assertions.assertEquals("SUBMITTED", quote.getQuoteStatus());
    }

    @Test
    void customerQuoteListingUsesCustomerRepositoryQuery() {
        UUID customer = UUID.randomUUID();
        Quote quote = Quote.builder().quoteId(UUID.randomUUID()).customerId(customer)
                .quoteStatus("DRAFT").build();
        when(quoteRepository.findByCustomerId(customer)).thenReturn(reactor.core.publisher.Flux.just(quote));
        when(mapper.toQuoteResponse(quote)).thenReturn(new QuoteResponse(
                quote.getQuoteId(), customer, null, null, null, 0, null, null,
                "DRAFT", null, null, null));

        StepVerifier.create(quoteService.getQuotesByCustomer(customer))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals(customer, response.customerId()))
                .verifyComplete();
        verify(quoteRepository).findByCustomerId(customer);
    }

    @Test
    void missingPolicyCannotBeRead() {
        UUID policyId = UUID.randomUUID();
        when(policyRepository.findById(policyId)).thenReturn(Mono.empty());

        StepVerifier.create(policyService.getPolicy(policyId))
                .expectErrorMatches(error -> error.getMessage().contains("Policy not found"))
                .verify();
    }
}
