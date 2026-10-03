package com.intellisure.recoveryservice.controller;

import com.intellisure.recoveryservice.dto.RecoveryEstimationRequest;
import com.intellisure.recoveryservice.dto.RecoveryEstimationResponse;
import com.intellisure.recoveryservice.service.RecoveryEstimationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/recovery")
@RequiredArgsConstructor
public class RecoveryEstimationController {

    private final RecoveryEstimationService estimationService;

    @PostMapping("/estimate")
    public Mono<RecoveryEstimationResponse> estimateRecovery(@Valid @RequestBody RecoveryEstimationRequest request) {
        return Mono.fromFuture(estimationService.estimateRecoveryAsync(request));
    }
}