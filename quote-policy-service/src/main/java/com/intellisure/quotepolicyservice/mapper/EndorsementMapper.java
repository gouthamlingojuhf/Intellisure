package com.intellisure.quotepolicyservice.mapper;

import com.intellisure.quotepolicyservice.dto.EndorsementCoverageResponse;
import com.intellisure.quotepolicyservice.dto.EndorsementResponse;
import com.intellisure.quotepolicyservice.entity.Endorsement;
import com.intellisure.quotepolicyservice.entity.EndorsementCoverage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EndorsementMapper {

    @Mapping(target = "coverages", expression = "java(mapCoverages(coverages))")
    EndorsementResponse toResponse(Endorsement endorsement, List<EndorsementCoverage> coverages);

    default List<EndorsementCoverageResponse> mapCoverages(List<EndorsementCoverage> coverages) {
        return coverages == null ? List.of() : coverages.stream()
                .map(this::toCoverageResponse)
                .collect(Collectors.toList());
    }

    default EndorsementCoverageResponse toCoverageResponse(EndorsementCoverage coverage) {
        return new EndorsementCoverageResponse(
                coverage.getEndorsementCoverageId(),
                coverage.getEndorsementId(),
                coverage.getCoverageCode(),
                coverage.getCoverageName(),
                coverage.getLimitAmount(),
                coverage.getDeductibleAmount(),
                coverage.getCoveragePremium(),
                coverage.getConditions(),
                coverage.getExclusions(),
                coverage.getWaitingPeriodDays(),
                coverage.getOperation() != null ? coverage.getOperation().name() : null,
                coverage.getCreatedAt()
        );
    }
}