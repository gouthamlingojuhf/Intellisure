package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.SubjectivityResponse;
import com.intellisure.quotepolicyservice.entity.Subjectivity;
import com.intellisure.quotepolicyservice.enums.SubjectivityStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubjectivityMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "enumToString")
    @Mapping(target = "evidenceDocumentIds", source = "evidenceDocumentIds", qualifiedByName = "parseDocuments")
    SubjectivityResponse toResponse(Subjectivity subjectivity);

    @Named("enumToString")
    default String enumToString(Enum<?> e) {
        return e != null ? e.name() : null;
    }

    @Named("parseDocuments")
    default List<java.util.UUID> parseDocuments(String json) {
        if (json == null || json.isBlank()) return List.of();
        // Simplified - in production use ObjectMapper
        return List.of();
    }
}