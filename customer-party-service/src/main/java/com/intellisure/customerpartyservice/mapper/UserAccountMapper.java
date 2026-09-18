package com.intellisure.customerpartyservice.mapper;

import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserAccountMapper {
    UserResponse toUserResponse(UserAccount userAccount);
}
