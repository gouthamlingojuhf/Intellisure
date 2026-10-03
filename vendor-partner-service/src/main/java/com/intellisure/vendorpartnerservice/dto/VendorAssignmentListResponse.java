package com.intellisure.vendorpartnerservice.dto;

import java.util.List;

public record VendorAssignmentListResponse(
        List<VendorAssignmentResponse> items,
        Integer page,
        Integer size,
        Long totalElements
) {}