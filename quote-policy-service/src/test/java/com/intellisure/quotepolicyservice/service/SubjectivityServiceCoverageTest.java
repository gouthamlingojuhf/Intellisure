package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.AddSubjectivityRequest;
import com.intellisure.quotepolicyservice.dto.SatisfySubjectivityRequest;
import com.intellisure.quotepolicyservice.dto.WaiveSubjectivityRequest;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.Subjectivity;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.SubjectivityStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.mapper.SubjectivityMapper;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.SubjectivityRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubjectivityServiceCoverageTest {
    private SubjectivityRepository subjectivityRepository;
    private QuoteRepository quoteRepository;
    private SecurityActorService security;
    private SubjectivityService service;
    private final UUID actor = TestFixtures.UNDERWRITER_ID;

    @BeforeEach
    void setUp() {
        subjectivityRepository = mock(SubjectivityRepository.class);
        quoteRepository = mock(QuoteRepository.class);
        SubjectivityMapper mapper = mock(SubjectivityMapper.class);
        security = mock(SecurityActorService.class);
        service = new SubjectivityService(subjectivityRepository, quoteRepository, mapper, security);
        lenient().when(mapper.toResponse(any())).thenReturn(mock(com.intellisure.quotepolicyservice.dto.SubjectivityResponse.class));
        lenient().when(security.currentUserId()).thenReturn(Mono.just(actor));
        lenient().when(security.assertCustomerAccess(any(UUID.class))).thenReturn(Mono.empty());
        lenient().when(subjectivityRepository.save(any(Subjectivity.class)))
                .thenAnswer(i -> Mono.just(i.getArgument(0)));
    }

    private Quote quote(QuoteStatus status) {
        Quote q = TestFixtures.quote(status);
        when(quoteRepository.findById(q.getQuoteId())).thenReturn(Mono.just(q));
        return q;
    }

    private Subjectivity subjectivity(SubjectivityStatus status, UUID quoteId) {
        return Subjectivity.builder().subjectivityId(UUID.randomUUID()).quoteId(quoteId)
                .subjectivityCode("DOC").description("Document required").status(status)
                .evidenceDocumentIds("[]").createdAt(TestFixtures.NOW).updatedAt(TestFixtures.NOW).build();
    }

    @Test
    void addsForEveryReviewableQuoteStatus() {
        AddSubjectivityRequest request = new AddSubjectivityRequest("DOC", "Document required");
        for (QuoteStatus status : List.of(QuoteStatus.IN_REVIEW, QuoteStatus.NEEDS_INFORMATION, QuoteStatus.RISK_ASSESSMENT)) {
            Quote q = quote(status);
            StepVerifier.create(service.addSubjectivity(q.getQuoteId(), request))
                    .expectNextCount(1).verifyComplete();
        }
        Quote draft = quote(QuoteStatus.DRAFT);
        StepVerifier.create(service.addSubjectivity(draft.getQuoteId(), request))
                .expectError(BusinessException.class).verify();
        for (QuoteStatus invalid : List.of(QuoteStatus.SUBMITTED, QuoteStatus.QUOTED, QuoteStatus.ACCEPTED)) {
            Quote invalidQuote = quote(invalid);
            StepVerifier.create(service.addSubjectivity(invalidQuote.getQuoteId(), request))
                    .expectError(BusinessException.class).verify();
        }
    }

    @Test
    void satisfiesAndWaivesOpenSubjectivity() {
        Quote q = quote(QuoteStatus.IN_REVIEW);
        Subjectivity s = subjectivity(SubjectivityStatus.OPEN, q.getQuoteId());
        when(subjectivityRepository.findById(s.getSubjectivityId())).thenReturn(Mono.just(s));
        StepVerifier.create(service.satisfySubjectivity(s.getSubjectivityId(),
                        new SatisfySubjectivityRequest(actor, List.of(UUID.randomUUID()))))
                .expectNextCount(1).verifyComplete();
        s.setStatus(SubjectivityStatus.OPEN);
        StepVerifier.create(service.waiveSubjectivity(s.getSubjectivityId(),
                        new WaiveSubjectivityRequest(actor, "Not applicable")))
                .expectNextCount(1).verifyComplete();
    }

    @Test
    void enforcesActorAndOpenStatus() {
        Quote q = quote(QuoteStatus.IN_REVIEW);
        Subjectivity s = subjectivity(SubjectivityStatus.OPEN, q.getQuoteId());
        when(subjectivityRepository.findById(s.getSubjectivityId())).thenReturn(Mono.just(s));
        StepVerifier.create(service.satisfySubjectivity(s.getSubjectivityId(),
                        new SatisfySubjectivityRequest(TestFixtures.OTHER_UNDERWRITER_ID, List.of())))
                .expectError(AccessDeniedBusinessException.class).verify();
        StepVerifier.create(service.waiveSubjectivity(s.getSubjectivityId(),
                        new WaiveSubjectivityRequest(TestFixtures.OTHER_UNDERWRITER_ID, "reason")))
                .expectError(AccessDeniedBusinessException.class).verify();
        s.setStatus(SubjectivityStatus.SATISFIED);
        StepVerifier.create(service.waiveSubjectivity(s.getSubjectivityId(),
                        new WaiveSubjectivityRequest(actor, "reason")))
                .expectError(BusinessException.class).verify();
    }

    @Test
    void listsAfterOwnershipCheck() {
        Quote q = quote(QuoteStatus.IN_REVIEW);
        when(subjectivityRepository.findAllByQuoteId(q.getQuoteId())).thenReturn(Flux.just(subjectivity(SubjectivityStatus.OPEN, q.getQuoteId())));
        StepVerifier.create(service.getSubjectivities(q.getQuoteId())).expectNextCount(1).verifyComplete();
        when(security.assertCustomerAccess(q.getCustomerId())).thenReturn(Mono.error(new AccessDeniedBusinessException("denied")));
        StepVerifier.create(service.getSubjectivities(q.getQuoteId())).expectError(AccessDeniedBusinessException.class).verify();
    }
}
