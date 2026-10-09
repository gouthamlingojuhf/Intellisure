package com.intellisure.quotepolicyservice.security;

import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.mapper.PolicyMapper;
import com.intellisure.quotepolicyservice.mapper.QuoteMapper;
import com.intellisure.quotepolicyservice.repository.PolicyCoverageRepository;
import com.intellisure.quotepolicyservice.repository.PolicyRepository;
import com.intellisure.quotepolicyservice.repository.QuoteCoverageRepository;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.QuoteVersionRepository;
import com.intellisure.quotepolicyservice.repository.SubjectivityRepository;
import com.intellisure.quotepolicyservice.repository.UnderwritingDecisionRepository;
import com.intellisure.quotepolicyservice.service.PolicyService;
import com.intellisure.quotepolicyservice.service.QuoteService;
import com.intellisure.quotepolicyservice.service.RatingService;
import com.intellisure.quotepolicyservice.service.assignment.UnderwriterAssignmentService;
import com.intellisure.quotepolicyservice.testsupport.TestAssertions;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuotePolicyOwnershipSecurityTest {

    @Mock private QuoteRepository quoteRepository;
    @Mock private QuoteCoverageRepository quoteCoverageRepository;
    @Mock private R2dbcEntityTemplate entityTemplate;
    @Mock private UnderwriterAssignmentService underwriterAssignmentService;
    @Mock private UnderwritingDecisionRepository underwritingDecisionRepository;
    @Mock private QuoteVersionRepository quoteVersionRepository;
    @Mock private SubjectivityRepository subjectivityRepository;
    @Mock private RatingService ratingService;
    @Mock private PolicyRepository policyRepository;
    @Mock private PolicyCoverageRepository policyCoverageRepository;

    private QuoteService quoteService;
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        SecurityActorService securityActorService = new SecurityActorService();
        quoteService = new QuoteService(
                quoteRepository,
                quoteCoverageRepository,
                entityTemplate,
                new QuoteMapper(),
                underwriterAssignmentService,
                underwritingDecisionRepository,
                securityActorService,
                quoteVersionRepository,
                subjectivityRepository,
                ratingService
        );
        policyService = new PolicyService(
                quoteRepository,
                quoteCoverageRepository,
                policyRepository,
                policyCoverageRepository,
                entityTemplate,
                new PolicyMapper(),
                securityActorService
        );
    }

    @Test
    void policyholderCannotReadAnotherCustomersQuote() {
        Quote quote = TestFixtures.quote(com.intellisure.quotepolicyservice.enums.QuoteStatus.DRAFT);
        when(quoteRepository.findById(quote.getQuoteId())).thenReturn(Mono.just(quote));

        StepVerifier.create(
                        Mono.defer(() -> quoteService.getQuoteById(quote.getQuoteId()))
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                        policyholderToken(UUID.randomUUID())
                                ))
                )
                .expectErrorSatisfies(error -> TestAssertions.errorIs(
                        com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException.class,
                        "The authenticated customer does not own this resource",
                        error
                ))
                .verify();
    }

    @Test
    void policyholderCannotReadAnotherCustomersPolicy() {
        Policy policy = TestFixtures.policy(com.intellisure.quotepolicyservice.enums.PolicyStatus.IN_FORCE);
        when(policyRepository.findById(policy.getPolicyId())).thenReturn(Mono.just(policy));

        StepVerifier.create(
                        Mono.defer(() -> policyService.getPolicyById(policy.getPolicyId()))
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(
                                        policyholderToken(UUID.randomUUID())
                                ))
                )
                .expectErrorSatisfies(error -> TestAssertions.errorIs(
                        com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException.class,
                        "The authenticated customer does not own this resource",
                        error
                ))
                .verify();
    }

    private JwtAuthenticationToken policyholderToken(UUID customerId) {
        return new JwtAuthenticationToken(
                Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject(UUID.randomUUID().toString())
                        .claim("customerId", customerId.toString())
                        .build(),
                List.of(new SimpleGrantedAuthority("ROLE_POLICYHOLDER"))
        );
    }
}
