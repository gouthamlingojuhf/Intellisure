package com.intellisure.claimsservice.service;

import com.intellisure.claimsservice.entity.Payment;
import com.intellisure.claimsservice.entity.ClaimFinancials;
import com.intellisure.claimsservice.exception.ResourceNotFoundException;
import com.intellisure.claimsservice.exception.BusinessException;
import com.intellisure.claimsservice.repository.PaymentRepository;
import com.intellisure.claimsservice.repository.ClaimFinancialsRepository;
import com.intellisure.claimsservice.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ClaimFinancialsRepository financialsRepository;
    private final ClaimRepository claimRepository;
    private final R2dbcEntityTemplate entityTemplate;

    public Flux<Payment> getPayments(UUID claimId) {
        return paymentRepository.findAllByClaimId(claimId);
    }

    public Mono<Payment> getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment not found: " + paymentId)));
    }

    public Mono<Payment> getPaymentByReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment not found with reference: " + paymentReference)));
    }

    public Mono<Payment> createPayment(UUID claimId, String paymentReference, java.math.BigDecimal amount,
                                       String paymentType, String paymentMethod, String referenceNumber,
                                       String notes, UUID createdBy) {
        return claimRepository.findById(claimId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Claim not found: " + claimId)))
                .flatMap(claim -> {
                    Payment payment = new Payment();
                    payment.setPaymentId(UUID.randomUUID());
                    payment.setClaimId(claimId);
                    payment.setPaymentReference(paymentReference);
                    payment.setAmount(amount);
                    payment.setPaymentDate(LocalDateTime.now());
                    payment.setPaymentType(paymentType);
                    payment.setPaymentMethod(paymentMethod);
                    payment.setStatus("PENDING");
                    payment.setReferenceNumber(referenceNumber);
                    payment.setNotes(notes);
                    payment.setCreatedBy(createdBy);
                    payment.setCreatedAt(LocalDateTime.now());
                    payment.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.insert(payment).thenReturn(payment)
                            .flatMap(saved -> {
                                return financialsRepository.findByClaimId(claimId)
                                        .flatMap(financials -> {
                                            financials.setPaidAmount(financials.getPaidAmount().add(amount));
                                            financials.setOutstandingReserve(financials.getReserveAmount().subtract(financials.getPaidAmount()));
                                            financials.setLastUpdatedAt(LocalDateTime.now());
                                            financials.setUpdatedAt(LocalDateTime.now());
                                            return entityTemplate.update(financials).thenReturn(financials);
                                        })
                                        .thenReturn(saved);
                                });
                    });
    }

    public Mono<Payment> updatePaymentStatus(UUID paymentId, String status) {
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment not found: " + paymentId)))
                .flatMap(payment -> {
                    payment.setStatus(status);
                    payment.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(payment).thenReturn(payment);
                });
    }

    public Mono<Payment> completePayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Payment not found: " + paymentId)))
                .flatMap(payment -> {
                    payment.setStatus("COMPLETED");
                    payment.setUpdatedAt(LocalDateTime.now());
                    return entityTemplate.update(payment).thenReturn(payment);
                });
    }

    public Flux<Payment> getPendingPayments(UUID claimId) {
        return paymentRepository.findAllByClaimId(claimId)
                .filter(p -> "PENDING".equals(p.getStatus()));
    }
}