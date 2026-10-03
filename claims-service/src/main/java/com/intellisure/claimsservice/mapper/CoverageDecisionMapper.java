package com.intellisure.claimsservice.mapper;

import com.intellisure.claimsservice.dto.CoverageDecisionResponse;
import com.intellisure.claimsservice.entity.CoverageDecision;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CoverageDecisionMapper {
    CoverageDecisionMapper INSTANCE = Mappers.getMapper(CoverageDecisionMapper.class);

    CoverageDecisionResponse toResponse(CoverageDecision decision);
}