package com.intellisure.vendorpartnerservice.dto;

import java.util.List;

public record VendorOnboardingListResponse(
        List<VendorOnboardingResponse> items,
        Integer page,
        Integer size,
        Long totalElements
) {}