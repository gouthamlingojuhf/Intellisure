package com.intellisure.riskunderwritingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import com.intellisure.riskunderwritingservice.mapper.RiskAssessmentMapper;
import com.intellisure.riskunderwritingservice.mapper.RiskEvidenceJsonMapper;
import com.intellisure.riskunderwritingservice.entity.RiskAssessment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RiskMapperCoverageTest {
    @Test
    void evidenceMapperHandlesEmptyValidAndInvalidPayloads() {
        RiskEvidenceJsonMapper mapper = new RiskEvidenceJsonMapper(new ObjectMapper());
        UUID id = UUID.randomUUID();
        assertNull(mapper.toJson(null));
        assertNull(mapper.toJson(List.of()));
        String json = mapper.toJson(List.of(id));
        assertEquals(List.of(id), mapper.fromJson(json));
        assertEquals(List.of(), mapper.fromJson(null));
        assertEquals(List.of(), mapper.fromJson(" "));
        assertThrows(BusinessException.class, () -> mapper.fromJson("not-json"));
    }

    @Test
    void riskAssessmentMapperMapsEntityFieldsAndRejectsNullByContract() {
        RiskAssessmentMapper mapper = new RiskAssessmentMapper();
        RiskAssessment assessment = RiskAssessment.builder().assessmentId(UUID.randomUUID()).assessmentNumber("RA-1").build();
        assertEquals(assessment.getAssessmentId(), mapper.toResponse(assessment).assessmentId());
        assertThrows(NullPointerException.class, () -> mapper.toResponse(null));
    }
}
