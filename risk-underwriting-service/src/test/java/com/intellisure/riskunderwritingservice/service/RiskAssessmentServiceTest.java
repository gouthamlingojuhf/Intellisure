package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.request.CreateRiskAssessmentRequest;
import com.intellisure.riskunderwritingservice.dto.response.RiskAssessmentResponse;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.enums.RiskAssessmentStatus;
import com.intellisure.riskunderwritingservice.exception.AccessDeniedBusinessException;
import com.intellisure.riskunderwritingservice.mapper.RiskAssessmentMapper;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import com.intellisure.riskunderwritingservice.security.SecurityActorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {
    @Mock private RiskAssessmentRepository assessmentRepository;
    @Mock private R2dbcEntityTemplate entityTemplate;
    @Mock private RiskAssessmentMapper mapper;
    @Mock private SecurityActorService securityActorService;

    @InjectMocks private RiskAssessmentService service;

    @Test
    void startAssessmentRejectsNonAssignedUser() {
        UUID assessmentId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId(assessmentId)
                .quoteId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .assessmentType("UNDERWRITING")
                .assessmentDate(LocalDate.now())
                .location("New York")
                .businessOperations("Retail")
                .status(RiskAssessmentStatus.DRAFT)
                .assignedUnderwriterId(otherUserId)
                .assignedRiskEngineerId(otherUserId)
                .createdBy(otherUserId)
                .build();

        when(assessmentRepository.findById(assessmentId)).thenReturn(Mono.just(assessment));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(currentUserId));

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                Jwt.withTokenValue("token")
                        .header("alg", "none")
                        .subject(currentUserId.toString())
                        .build(),
                List.of(new SimpleGrantedAuthority("ROLE_UNDERWRITER")),
                currentUserId.toString()
        );

        Mono<RiskAssessmentResponse> request = Mono.defer(() -> service.startAssessment(assessmentId))
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        StepVerifier.create(request)
                .expectErrorSatisfies(error -> assertInstanceOf(AccessDeniedBusinessException.class, error))
                .verify();
    }

    @Test
    void createAssessmentAcceptsCurrentRequestShape() {
        UUID quoteId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();

        CreateRiskAssessmentRequest request = new CreateRiskAssessmentRequest(
                quoteId,
                null,
                customerId,
                "UNDERWRITING",
                LocalDate.of(2026, 9, 30),
                "New York",
                "Retail operations",
                new BigDecimal("500000"),
                new BigDecimal("250000"),
                30,
                new BigDecimal("2000000"),
                2,
                new BigDecimal("15000"),
                createdBy
        );

        when(assessmentRepository.existsByQuoteId(quoteId)).thenReturn(Mono.just(false));

        StepVerifier.create(service.createAssessment(request))
                .expectError();
    }

    @Test
    void underwriterQueueRejectsAnotherUsersQueue() {
        UUID currentUserId = UUID.randomUUID();
        UUID requestedUserId = UUID.randomUUID();

        when(securityActorService.hasRole("ADMIN")).thenReturn(Mono.just(false));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(currentUserId));

        StepVerifier.create(service.getAssignedUnderwriterQueue(requestedUserId))
                .expectErrorSatisfies(error -> assertInstanceOf(
                        AccessDeniedBusinessException.class,
                        error
                ))
                .verify();
    }

    @Test
    void underwriterQueueLoadsOnlyForAuthenticatedUser() {
        UUID currentUserId = UUID.randomUUID();
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId(UUID.randomUUID())
                .quoteId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status(RiskAssessmentStatus.UNDER_REVIEW)
                .assignedUnderwriterId(currentUserId)
                .build();

        when(securityActorService.hasRole("ADMIN")).thenReturn(Mono.just(false));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(currentUserId));
        when(assessmentRepository.findAllByAssignedUnderwriterId(currentUserId))
                .thenReturn(Flux.just(assessment));
        when(mapper.toResponse(assessment)).thenReturn(new RiskAssessmentResponse(
                assessment.getAssessmentId(),
                null,
                assessment.getQuoteId(),
                null,
                assessment.getCustomerId(),
                null,
                assessment.getStatus(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                assessment.getAssignedUnderwriterId(),
                null,
                null,
                null,
                null,
                null
        ));

        StepVerifier.create(service.getAssignedUnderwriterQueue(currentUserId))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void riskEngineerQueueRejectsAnotherUsersQueue() {
        UUID currentUserId = UUID.randomUUID();
        UUID requestedUserId = UUID.randomUUID();

        when(securityActorService.hasRole("ADMIN")).thenReturn(Mono.just(false));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(currentUserId));

        StepVerifier.create(service.getAssignedRiskEngineerQueue(requestedUserId))
                .expectErrorSatisfies(error -> assertInstanceOf(
                        AccessDeniedBusinessException.class,
                        error
                ))
                .verify();
    }

    @Test
    void riskEngineerQueueLoadsOnlyForAuthenticatedUser() {
        UUID currentUserId = UUID.randomUUID();
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId(UUID.randomUUID())
                .quoteId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .status(RiskAssessmentStatus.RISK_ENGINEERING_REQUIRED)
                .assignedRiskEngineerId(currentUserId)
                .build();

        when(securityActorService.hasRole("ADMIN")).thenReturn(Mono.just(false));
        when(securityActorService.currentUserId()).thenReturn(Mono.just(currentUserId));
        when(assessmentRepository.findAllByAssignedRiskEngineerId(currentUserId))
                .thenReturn(Flux.just(assessment));
        when(mapper.toResponse(assessment)).thenReturn(new RiskAssessmentResponse(
                assessment.getAssessmentId(),
                null,
                assessment.getQuoteId(),
                null,
                assessment.getCustomerId(),
                null,
                assessment.getStatus(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                assessment.getAssignedRiskEngineerId(),
                null,
                null,
                null,
                null
        ));

        StepVerifier.create(service.getAssignedRiskEngineerQueue(currentUserId))
                .expectNextCount(1)
                .verifyComplete();
    }
}
