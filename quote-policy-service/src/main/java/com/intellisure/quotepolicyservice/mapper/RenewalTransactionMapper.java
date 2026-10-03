package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.RenewalCoverageResponse;
import com.intellisure.quotepolicyservice.dto.RenewalSubjectivityResponse;
import com.intellisure.quotepolicyservice.dto.RenewalTransactionResponse;
import com.intellisure.quotepolicyservice.entity.RenewalTransaction;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface RenewalTransactionMapper {

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