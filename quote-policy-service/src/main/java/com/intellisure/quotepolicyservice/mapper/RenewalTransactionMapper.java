package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.RenewalCoverageResponse;
import com.intellisure.quotepolicyservice.dto.RenewalSubjectivityResponse;
import com.intellisure.quotepolicyservice.dto.RenewalTransactionResponse;
import com.intellisure.quotepolicyservice.entity.RenewalTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RenewalTransactionMapper {

    @Mapping(target = "proposedCoverages", expression = "java(mapCoverages(renewal.getProposedCoverageSnapshot()))")
    @Mapping(target = "subjectivities", expression = "java(mapSubjectivities(renewal.getSubjectivities()))")
    RenewalTransactionResponse toResponse(RenewalTransaction renewal);

    default List<RenewalCoverageResponse> mapCoverages(String json) {
        // Simplified - in production parse JSON
        return List.of();
    }

    default List<RenewalSubjectivityResponse> mapSubjectivities(String json) {
        // Simplified - in production parse JSON
        return List.of();
    }
}