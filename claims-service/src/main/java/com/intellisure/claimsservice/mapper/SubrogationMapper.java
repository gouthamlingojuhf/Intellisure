package com.intellisure.claimsservice.mapper;

import com.intellisure.claimsservice.dto.SubrogationResponse;
import com.intellisure.claimsservice.entity.Subrogation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SubrogationMapper {
    SubrogationMapper INSTANCE = Mappers.getMapper(SubrogationMapper.class);

    SubrogationResponse toResponse(Subrogation subrogation);
}