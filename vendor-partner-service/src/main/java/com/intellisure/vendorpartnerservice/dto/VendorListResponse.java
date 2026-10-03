package com.intellisure.vendorpartnerservice.dto;

import java.util.List;

public record VendorListResponse(
        List<VendorResponse> items,
        Integer page,
        Integer size,
        Long totalElements
) {}