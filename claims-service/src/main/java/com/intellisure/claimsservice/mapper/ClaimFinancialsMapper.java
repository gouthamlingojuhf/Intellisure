package com.intellisure.claimsservice.mapper;

import com.intellisure.claimsservice.dto.ClaimFinancialsResponse;
import com.intellisure.claimsservice.entity.ClaimFinancials;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ClaimFinancialsMapper {
    ClaimFinancialsMapper INSTANCE = Mappers.getMapper(ClaimFinancialsMapper.class);

    ClaimFinancialsResponse toResponse(ClaimFinancials financials);
}