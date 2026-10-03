package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.response.ImportedUnderwritingResultResponse;
import com.intellisure.quotepolicyservice.service.UnderwritingImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
@Tag(
        name = "Underwriting Integration",
        description = "Imports authoritative underwriting results"
)
public class UnderwritingIntegrationController {

    private final UnderwritingImportService service;

    @PatchMapping(
            "/{quoteId}/import-underwriting-result"
    )
    @Operation(
            summary = "Import authoritative underwriting result",
            description = """
                    Retrieves the completed underwriting result from
                    Risk & Underwriting Service and stores a local
                    decision snapshot for the quote transaction.
                    """
    )
    public Mono<ImportedUnderwritingResultResponse>
    importUnderwritingResult(
            @PathVariable UUID quoteId,

            @RequestHeader(
                    value = HttpHeaders.AUTHORIZATION,
                    required = false
            )
            String authorizationHeader
    ) {
        return service.importResult(
                quoteId,
                authorizationHeader
        );
    }
}