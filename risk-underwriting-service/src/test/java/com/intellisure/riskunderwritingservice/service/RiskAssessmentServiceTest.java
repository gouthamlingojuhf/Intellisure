package com.intellisure.riskunderwritingservice.service;

import com.intellisure.riskunderwritingservice.dto.CreateRiskAssessmentRequest;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import com.intellisure.riskunderwritingservice.repository.RiskAssessmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceTest {
    @Mock RiskAssessmentRepository repository;
    @InjectMocks RiskAssessmentService service;

    @Test
    void createsDraftAssessmentWithRequestFields() {
        UUID customer = UUID.randomUUID(), creator = UUID.randomUUID();
        when(repository.save(any(RiskAssessment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        CreateRiskAssessmentRequest request = new CreateRiskAssessmentRequest(
                UUID.randomUUID(), UUID.randomUUID(), customer, "UNDERWRITING",
                "NY", "retail", new BigDecimal("42"), "summary");
        StepVerifier.create(service.createAssessment(request, creator))
                .assertNext(r -> { assertEquals(customer, r.customerId()); assertEquals("DRAFT", r.status());
                    assertEquals(new BigDecimal("42"), r.riskScore()); })
                .verifyComplete();
        ArgumentCaptor<RiskAssessment> captor = ArgumentCaptor.forClass(RiskAssessment.class);
        verify(repository).save(captor.capture());
        assertEquals(creator, captor.getValue().getCreatedBy());
    }

    @Test
    void propagatesRepositoryError() {
        when(repository.save(any())).thenReturn(Mono.error(new RuntimeException("write failed")));
        StepVerifier.create(service.createAssessment(
                new CreateRiskAssessmentRequest(null, null, UUID.randomUUID(), "TYPE", null, null, null, null),
                UUID.randomUUID())).expectErrorMessage("write failed").verify();
    }
}
