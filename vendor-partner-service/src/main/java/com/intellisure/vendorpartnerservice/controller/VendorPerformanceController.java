package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.RecordVendorPerformanceRequest;
import com.intellisure.vendorpartnerservice.dto.VendorPerformanceResponse;
import com.intellisure.vendorpartnerservice.service.VendorPerformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorPerformanceController {

    private final VendorPerformanceService performanceService;

    @PostMapping("/{vendorId}/performance")
    public Mono<VendorPerformanceResponse> recordPerformance(@PathVariable UUID vendorId, 
                                                             @Valid @RequestBody RecordVendorPerformanceRequest request) {
        return performanceService.recordPerformance(request);
    }

    @GetMapping("/{vendorId}/performance")
    public Flux<VendorPerformanceResponse> getVendorPerformance(@PathVariable UUID vendorId,
                                                                 @RequestParam(required = false) LocalDateTime fromDate,
                                                                 @RequestParam(required = false) LocalDateTime toDate) {
        if (fromDate != null && toDate != null) {
            return performanceService.getVendorPerformance(vendorId, fromDate, toDate);
        }
        return performanceService.getVendorPerformance(vendorId);
    }
}