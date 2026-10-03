package com.intellisure.recoveryservice.dto;

import java.util.List;

public record RecoveryCaseListResponse(
        List<RecoveryCaseResponse> items,
        Integer page,
        Integer size,
        Long totalElements
) {}