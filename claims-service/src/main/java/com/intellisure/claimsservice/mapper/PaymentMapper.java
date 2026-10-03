package com.intellisure.claimsservice.mapper;

import com.intellisure.claimsservice.dto.PaymentResponse;
import com.intellisure.claimsservice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentResponse toResponse(Payment payment);
}