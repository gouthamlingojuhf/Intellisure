package com.intellisure.claimsservice.mapper;

import com.intellisure.claimsservice.dto.BusinessIncomeResponse;
import com.intellisure.claimsservice.entity.BusinessIncome;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BusinessIncomeMapper {
    BusinessIncomeMapper INSTANCE = Mappers.getMapper(BusinessIncomeMapper.class);

    BusinessIncomeResponse toResponse(BusinessIncome bi);
}