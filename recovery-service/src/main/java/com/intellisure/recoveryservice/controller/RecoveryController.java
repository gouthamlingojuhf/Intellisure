package com.intellisure.recoveryservice.controller;

import com.intellisure.recoveryservice.dto.InitiateRecoveryRequest;
import com.intellisure.recoveryservice.dto.RecoveryCaseResponse;
import com.intellisure.recoveryservice.service.RecoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.UUID;

@RestController
@RequestMapping("/api/recovery")
@RequiredArgsConstructor
public class RecoveryController {

    private final RecoveryService recoveryService;

    @PostMapping
    public Mono<RecoveryCaseResponse> initiateRecovery(@Valid @RequestBody InitiateRecoveryRequest request) {
        return recoveryService.initiateRecovery(request);
    }

    @PostMapping("/cases")
    public Mono<RecoveryCaseResponse> createCase(@Valid @RequestBody InitiateRecoveryRequest request) {
        return recoveryService.initiateRecovery(request);
    }

    @GetMapping("/cases")
    public Flux<RecoveryCaseResponse> cases(@RequestParam(required = false) UUID customerId) {
        return recoveryService.getCases(customerId);
    }

    @GetMapping("/cases/{recoveryCaseId}")
    public Mono<RecoveryCaseResponse> getCase(@PathVariable UUID recoveryCaseId) {
        return recoveryService.getCase(recoveryCaseId);
    }
}
