package com.intellisure.claimsservice.mapper;

import com.intellisure.claimsservice.dto.SalvageResponse;
import com.intellisure.claimsservice.entity.Salvage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SalvageMapper {
    SalvageMapper INSTANCE = Mappers.getMapper(SalvageMapper.class);

    SalvageResponse toResponse(Salvage salvage);
}