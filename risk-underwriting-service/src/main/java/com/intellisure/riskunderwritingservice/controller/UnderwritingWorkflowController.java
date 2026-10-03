package com.intellisure.riskunderwritingservice.controller;

import com.intellisure.riskunderwritingservice.dto.request.AssignReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.CreateSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.ResolveReferralRequest;
import com.intellisure.riskunderwritingservice.dto.request.SubmitSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.VerifySubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.request.WaiveSubjectivityRequest;
import com.intellisure.riskunderwritingservice.dto.response.SubjectivityResponse;
import com.intellisure.riskunderwritingservice.dto.response.UnderwritingReferralResponse;
import com.intellisure.riskunderwritingservice.service.UnderwritingWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(
        name = "Underwriting Workflow",
        description = "Underwriting referrals and subjectivities"
)
public class UnderwritingWorkflowController {

    private final UnderwritingWorkflowService service;

    /*
     * ---------------------------------------------------------
     * REFERRALS
     * ---------------------------------------------------------
     */

    @PostMapping(
            "/risk-assessments/{assessmentId}/referrals"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an underwriting referral")
    public Mono<UnderwritingReferralResponse>
    createReferral(
            @PathVariable UUID assessmentId,

            @Valid @RequestBody
            CreateReferralRequest request
    ) {
        return service.createReferral(
                assessmentId,
                request
        );
    }

    @PatchMapping(
            "/referrals/{referralId}/assign"
    )
    @Operation(summary = "Assign referral to higher authority")
    public Mono<UnderwritingReferralResponse>
    assignReferral(
            @PathVariable UUID referralId,

            @Valid @RequestBody
            AssignReferralRequest request
    ) {
        return service.assignReferral(
                referralId,
                request
        );
    }

    @PatchMapping(
            "/referrals/{referralId}/resolve"
    )
    @Operation(summary = "Resolve underwriting referral")
    public Mono<UnderwritingReferralResponse>
    resolveReferral(
            @PathVariable UUID referralId,

            @Valid @RequestBody
            ResolveReferralRequest request
    ) {
        return service.resolveReferral(
                referralId,
                request
        );
    }

    @GetMapping(
            "/risk-assessments/{assessmentId}/referrals"
    )
    @Operation(summary = "Get referral history")
    public Flux<UnderwritingReferralResponse>
    getReferrals(
            @PathVariable UUID assessmentId
    ) {
        return service.getReferrals(
                assessmentId
        );
    }

    /*
     * ---------------------------------------------------------
     * SUBJECTIVITIES
     * ---------------------------------------------------------
     */

    @PostMapping(
            "/risk-assessments/{assessmentId}/subjectivities"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a subjectivity")
    public Mono<SubjectivityResponse>
    createSubjectivity(
            @PathVariable UUID assessmentId,

            @Valid @RequestBody
            CreateSubjectivityRequest request
    ) {
        return service.createSubjectivity(
                assessmentId,
                request
        );
    }

    @PatchMapping(
            "/subjectivities/{subjectivityId}/submit"
    )
    @Operation(summary = "Submit subjectivity evidence")
    public Mono<SubjectivityResponse>
    submitSubjectivityEvidence(
            @PathVariable UUID subjectivityId,

            @Valid @RequestBody
            SubmitSubjectivityRequest request
    ) {
        return service.submitSubjectivityEvidence(
                subjectivityId,
                request
        );
    }

    @PatchMapping(
            "/subjectivities/{subjectivityId}/verify"
    )
    @Operation(summary = "Verify submitted subjectivity")
    public Mono<SubjectivityResponse>
    verifySubjectivity(
            @PathVariable UUID subjectivityId,

            @Valid @RequestBody
            VerifySubjectivityRequest request
    ) {
        return service.verifySubjectivity(
                subjectivityId,
                request
        );
    }

    @PatchMapping(
            "/subjectivities/{subjectivityId}/waive"
    )
    @Operation(summary = "Waive subjectivity")
    public Mono<SubjectivityResponse>
    waiveSubjectivity(
            @PathVariable UUID subjectivityId,

            @Valid @RequestBody
            WaiveSubjectivityRequest request
    ) {
        return service.waiveSubjectivity(
                subjectivityId,
                request
        );
    }

    @GetMapping(
            "/risk-assessments/{assessmentId}/subjectivities"
    )
    @Operation(summary = "Get assessment subjectivities")
    public Flux<SubjectivityResponse>
    getSubjectivities(
            @PathVariable UUID assessmentId
    ) {
        return service.getSubjectivities(
                assessmentId
        );
    }

    @GetMapping(
            "/risk-assessments/{assessmentId}/"
                    + "subjectivities/outstanding-bind"
    )
    @Operation(
            summary = "Get outstanding bind-blocking subjectivities"
    )
    public Flux<SubjectivityResponse>
    getOutstandingBindSubjectivities(
            @PathVariable UUID assessmentId
    ) {
        return service
                .getOutstandingBindSubjectivities(
                        assessmentId
                );
    }
}