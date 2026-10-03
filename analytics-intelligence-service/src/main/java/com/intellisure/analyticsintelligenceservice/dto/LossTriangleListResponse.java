package com.intellisure.analyticsintelligenceservice.dto;

import java.util.List;

public record LossTriangleListResponse(
        List<LossTriangleResponse> items,
        Integer accidentYear
) {}