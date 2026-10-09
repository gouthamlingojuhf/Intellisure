package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.*;
import com.intellisure.riskunderwritingservice.dto.response.RiskAssessmentResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.exception.ResourceNotFoundException;
import com.intellisure.riskunderwritingservice.mapper.RiskAssessmentMapper;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import com.intellisure.riskunderwritingservice.testsupport.EntityTemplateStubber;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceCoverageTest {
    @Mock RiskAssessmentRepository repository;
    @Mock R2dbcEntityTemplate entityTemplate;
    @Mock RiskAssessmentMapper mapper;
    @Mock SecurityActorService security;
    @InjectMocks RiskAssessmentService service;

    private final UUID id = UUID.randomUUID();
    private final UUID actor = UUID.randomUUID();
    private final RiskAssessmentResponse response = mock(RiskAssessmentResponse.class);

    private RiskAssessment assessment(RiskAssessmentStatus status) {
        return RiskAssessment.builder().assessmentId(id).assessmentNumber("RA-1").quoteId(UUID.randomUUID())
                .customerId(UUID.randomUUID()).assessmentType("UNDERWRITING").assessmentDate(LocalDate.now())
                .location("City").businessOperations("Retail").status(status).assignedUnderwriterId(actor)
                .assignedRiskEngineerId(actor).build();
    }

    private CreateRiskAssessmentRequest request() {
        return new CreateRiskAssessmentRequest(UUID.randomUUID(), null, UUID.randomUUID(), " underwriting ", LocalDate.now(),
                " City ", " Retail ", BigDecimal.TEN, BigDecimal.ONE, 2, BigDecimal.TEN, null, null, actor);
    }

    @Test
    void createStartAssignSubmitAndScoreAssessment() {
        when(mapper.toResponse(any(RiskAssessment.class))).thenReturn(response);
        when(repository.existsByQuoteId(any())).thenReturn(Mono.just(false));
        EntityTemplateStubber.stubInsert(entityTemplate, RiskAssessment.class);
        when(entityTemplate.update(any(RiskAssessment.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        StepVerifier.create(service.createAssessment(request())).expectNext(response).verifyComplete();
        CreateRiskAssessmentRequest populated = new CreateRiskAssessmentRequest(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), " underwriting ", LocalDate.now(),
                " City ", " Retail ", BigDecimal.TEN, BigDecimal.ONE, 2, BigDecimal.TEN, 1, BigDecimal.ONE, actor);
        StepVerifier.create(service.createAssessment(populated)).expectNext(response).verifyComplete();
        when(repository.existsByQuoteId(any())).thenReturn(Mono.just(true));
        StepVerifier.create(service.createAssessment(request())).expectError(BusinessException.class).verify();

        RiskAssessment draft = assessment(RiskAssessmentStatus.DRAFT);
        when(repository.findById(id)).thenReturn(Mono.just(draft));
        when(security.currentUserId()).thenReturn(Mono.just(actor));
        StepVerifier.create(service.startAssessment(id)).expectNext(response).verifyComplete();
        draft.setStatus(RiskAssessmentStatus.DRAFT);
        draft.setAssignedUnderwriterId(null);
        draft.setAssignedRiskEngineerId(actor);
        StepVerifier.create(service.startAssessment(id)).expectNext(response).verifyComplete();
        draft.setStatus(RiskAssessmentStatus.DRAFT);
        draft.setAssignedUnderwriterId(actor);
        draft.setAssignedRiskEngineerId(actor);
        StepVerifier.create(service.assignAssessment(id, new AssignAssessmentRequest(actor, actor))).expectNext(response).verifyComplete();
        draft.setStatus(RiskAssessmentStatus.COMPLETED);
        StepVerifier.create(service.assignAssessment(id, new AssignAssessmentRequest(actor, actor))).expectError(BusinessException.class).verify();

        draft.setStatus(RiskAssessmentStatus.IN_PROGRESS);
        draft.setAssignedUnderwriterId(actor);
        StepVerifier.create(service.submitForReview(id)).expectNext(response).verifyComplete();
        draft.setStatus(RiskAssessmentStatus.IN_PROGRESS);
        draft.setAssignedUnderwriterId(null);
        StepVerifier.create(service.submitForReview(id)).expectError(BusinessException.class).verify();

        draft.setStatus(RiskAssessmentStatus.UNDER_REVIEW);
        draft.setAssignedUnderwriterId(actor);
        when(security.currentUserId()).thenReturn(Mono.just(UUID.randomUUID()));
        StepVerifier.create(service.completeRiskScore(id, new CompleteRiskScoreRequest(BigDecimal.TEN, "summary"))).expectError().verify();
        when(security.currentUserId()).thenReturn(Mono.just(actor));
        for (BigDecimal score : new BigDecimal[]{BigDecimal.TEN, new BigDecimal("30"), new BigDecimal("50"), new BigDecimal("70"), new BigDecimal("90")}) {
            draft.setStatus(RiskAssessmentStatus.UNDER_REVIEW);
            StepVerifier.create(service.completeRiskScore(id, new CompleteRiskScoreRequest(score, " summary ")))
                    .expectNext(response).verifyComplete();
        }
        draft.setStatus(RiskAssessmentStatus.CANCELLED);
        StepVerifier.create(service.assignAssessment(id, new AssignAssessmentRequest(actor, actor))).expectError(BusinessException.class).verify();
    }

    @Test
    void readsStatusesAndQueuesWithAdminAccess() {
        RiskAssessment value = assessment(RiskAssessmentStatus.UNDER_REVIEW);
        when(mapper.toResponse(value)).thenReturn(response);
        when(repository.findById(id)).thenReturn(Mono.just(value));
        when(repository.findByAssessmentNumber("RA-1")).thenReturn(Mono.just(value));
        when(repository.findByQuoteId(value.getQuoteId())).thenReturn(Mono.just(value));
        when(repository.findAllByStatus(RiskAssessmentStatus.UNDER_REVIEW)).thenReturn(Flux.just(value));
        StepVerifier.create(service.getById(id)).expectNext(response).verifyComplete();
        StepVerifier.create(service.getByNumber("RA-1")).expectNext(response).verifyComplete();
        StepVerifier.create(service.getByQuoteId(value.getQuoteId())).expectNext(response).verifyComplete();
        StepVerifier.create(service.getByStatus(RiskAssessmentStatus.UNDER_REVIEW)).expectNext(response).verifyComplete();
        when(security.hasRole("ADMIN")).thenReturn(Mono.just(true));
        when(repository.findAllByAssignedUnderwriterId(actor)).thenReturn(Flux.just(value));
        when(repository.findAllByAssignedRiskEngineerId(actor)).thenReturn(Flux.just(value));
        StepVerifier.create(service.getAssignedUnderwriterQueue(actor)).expectNext(response).verifyComplete();
        StepVerifier.create(service.getAssignedRiskEngineerQueue(actor)).expectNext(response).verifyComplete();
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getById(id)).expectError(ResourceNotFoundException.class).verify();
        when(repository.findByAssessmentNumber("missing")).thenReturn(Mono.empty());
        StepVerifier.create(service.getByNumber("missing")).expectError(ResourceNotFoundException.class).verify();
        when(repository.findByQuoteId(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getByQuoteId(id)).expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void queueAccessAndStatusValidationRejectIncorrectActorsOrStates() {
        RiskAssessment value = assessment(RiskAssessmentStatus.COMPLETED);
        when(repository.findById(id)).thenReturn(Mono.just(value));
        when(security.currentUserId()).thenReturn(Mono.just(actor));
        StepVerifier.create(service.startAssessment(id)).expectError(BusinessException.class).verify();
        when(security.hasRole("ADMIN")).thenReturn(Mono.just(false));
        when(security.currentUserId()).thenReturn(Mono.just(UUID.randomUUID()));
        StepVerifier.create(service.getAssignedUnderwriterQueue(actor)).expectError().verify();
        StepVerifier.create(service.getAssignedRiskEngineerQueue(actor)).expectError().verify();
    }
}
