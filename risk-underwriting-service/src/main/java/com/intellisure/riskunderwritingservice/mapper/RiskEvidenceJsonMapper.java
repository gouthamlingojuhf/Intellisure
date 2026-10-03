package com.intellisure.riskunderwritingservice.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellisure.riskunderwritingservice.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RiskEvidenceJsonMapper {

    private final ObjectMapper objectMapper;

    public String toJson(List<UUID> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(
                    documentIds
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    "Unable to serialize evidence document IDs"
            );
        }
    }

    public List<UUID> fromJson(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<UUID>>() {
                    }
            );
        } catch (JsonProcessingException exception) {
            throw new BusinessException(
                    "Unable to read evidence document IDs"
            );
        }
    }
}