package com.intellisure.claimsservice.controller;

import com.intellisure.claimsservice.dto.PaymentResponse;
import com.intellisure.claimsservice.dto.CreatePaymentRequest;
import com.intellisure.claimsservice.dto.UpdatePaymentStatusRequest;
import com.intellisure.claimsservice.service.PaymentService;
import com.intellisure.claimsservice.mapper.PaymentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims/{claimId}/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping
    public Mono<ResponseEntity<PaymentResponse>> createPayment(
            @PathVariable UUID claimId,
            @Valid @RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(claimId, request.paymentReference(), request.amount(),
                        request.paymentType(), request.paymentMethod(), request.referenceNumber(),
                        request.notes(), request.createdBy())
                .map(paymentMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping
    public Flux<PaymentResponse> getPayments(@PathVariable UUID claimId) {
        return paymentService.getPayments(claimId)
                .map(paymentMapper::toResponse);
    }

    @GetMapping("/{paymentId}")
    public Mono<ResponseEntity<PaymentResponse>> getPayment(@PathVariable UUID claimId, @PathVariable UUID paymentId) {
        return paymentService.getPayment(paymentId)
                .map(paymentMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/reference/{paymentReference}")
    public Mono<ResponseEntity<PaymentResponse>> getPaymentByReference(
            @PathVariable UUID claimId, @PathVariable String paymentReference) {
        return paymentService.getPaymentByReference(paymentReference)
                .map(paymentMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PatchMapping("/{paymentId}/status")
    public Mono<ResponseEntity<PaymentResponse>> updatePaymentStatus(
            @PathVariable UUID claimId, @PathVariable UUID paymentId,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        return paymentService.updatePaymentStatus(paymentId, request.status())
                .map(paymentMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/{paymentId}/complete")
    public Mono<ResponseEntity<PaymentResponse>> completePayment(
            @PathVariable UUID claimId, @PathVariable UUID paymentId) {
        return paymentService.completePayment(paymentId)
                .map(paymentMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/pending")
    public Flux<PaymentResponse> getPendingPayments(@PathVariable UUID claimId) {
        return paymentService.getPendingPayments(claimId)
                .map(paymentMapper::toResponse);
    }
}