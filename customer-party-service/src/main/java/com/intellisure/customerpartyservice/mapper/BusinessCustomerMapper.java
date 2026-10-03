package com.intellisure.customerpartyservice.mapper;

import com.intellisure.customerpartyservice.dto.CustomerResponse;
import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BusinessCustomerMapper {
    CustomerResponse toCustomerResponse(BusinessCustomer businessCustomer);
}
