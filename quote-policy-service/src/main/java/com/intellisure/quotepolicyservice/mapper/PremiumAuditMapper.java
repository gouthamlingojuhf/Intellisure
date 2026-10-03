package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.PremiumAuditResponse;
import com.intellisure.quotepolicyservice.entity.PremiumAudit;
import com.intellisure.quotepolicyservice.enums.AuditType;
import com.intellisure.quotepolicyservice.enums.AuditStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface PremiumAuditMapper {

    @Mapping(target = "auditType", source = "auditType", qualifiedByName = "enumToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "enumToString")
    PremiumAuditResponse toResponse(PremiumAudit audit);

    @Named("enumToString")
    default String enumToString(Enum<?> e) {
        return e != null ? e.name() : null;
    }
}