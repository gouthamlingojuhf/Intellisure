package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.request.AssignUnderwriterRequest;
import com.intellisure.quotepolicyservice.dto.request.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.response.PolicyResponse;
import com.intellisure.quotepolicyservice.dto.response.QuoteResponse;
import com.intellisure.quotepolicyservice.service.PolicyService;
import com.intellisure.quotepolicyservice.service.QuoteService;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.RecordUnderwritingDecisionRequest;
import com.intellisure.quotepolicyservice.dto.response.UnderwritingDecisionResponse;
import com.intellisure.quotepolicyservice.dto.request.OfferQuoteTermsRequest;
import com.intellisure.quotepolicyservice.dto.request.RecordUnderwritingDecisionRequest;
import com.intellisure.quotepolicyservice.dto.response.UnderwritingDecisionResponse;

import com.intellisure.quotepolicyservice.dto.request.AcceptQuoteRequest;
import com.intellisure.quotepolicyservice.dto.request.DeclineQuoteRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
@Tag(
        name = "Quote Management",
        description = "Reactive APIs for insurance quote management"
)
public class QuoteController {

    private final QuoteService quoteService;
    private final PolicyService policyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a draft quote",
            description = "Creates a draft quote with requested coverages"
    )
    public Mono<QuoteResponse> createDraftQuote(
            @Valid @RequestBody CreateQuoteRequest request
    ) {
        return quoteService.createDraftQuote(request);
    }



    @PostMapping("/{quoteId}/underwriting-decisions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Record an underwriting decision",
            description = """
                Records an append-only underwriting decision.
                The quote must currently be IN_REVIEW.
                """
    )
    public Mono<UnderwritingDecisionResponse>
    recordUnderwritingDecision(
            @PathVariable UUID quoteId,
            @Valid @RequestBody
            RecordUnderwritingDecisionRequest request
    ) {
        return quoteService.recordUnderwritingDecision(
                quoteId,
                request
        );
    }


    @GetMapping("/{quoteId}/underwriting-decisions")
    @Operation(
            summary = "Get underwriting decision history",
            description = """
                Returns all underwriting decisions for the quote,
                ordered from newest to oldest.
                """
    )
    public Flux<UnderwritingDecisionResponse>
    getUnderwritingDecisionHistory(
            @PathVariable UUID quoteId
    ) {
        return quoteService
                .getUnderwritingDecisionHistory(quoteId);
    }



    @PatchMapping("/{quoteId}/offer-terms")
    @Operation(
            summary = "Offer quote terms",
            description = """
                Applies offered limits, deductibles, premiums,
                conditions and exclusions.

                An approved underwriting decision must exist.
                The quote changes from IN_REVIEW to QUOTED.
                """
    )
    public Mono<QuoteResponse> offerQuoteTerms(
            @PathVariable UUID quoteId,
            @Valid @RequestBody
            OfferQuoteTermsRequest request
    ) {
        return quoteService.offerQuoteTerms(
                quoteId,
                request
        );
    }

    @RequestMapping(value = "/{quoteId}/submit", method = {RequestMethod.POST, RequestMethod.PATCH})
    @Operation(
            summary = "Submit a draft quote",
            description = "Changes the quote from DRAFT to SUBMITTED"
    )
    public Mono<QuoteResponse> submitQuote(
            @PathVariable UUID quoteId
    ) {
        return quoteService.submitQuote(quoteId);
    }


    @RequestMapping(value = "/{quoteId}/accept", method = {RequestMethod.POST, RequestMethod.PATCH})
    @Operation(
            summary = "Accept quoted terms",
            description = """
                Customer accepts the offered quote terms.
                Changes the quote from QUOTED to ACCEPTED.
                """
    )
    public Mono<QuoteResponse> acceptQuote(
            @PathVariable UUID quoteId
    ) {
        return quoteService.acceptQuote(quoteId);
    }

    @PostMapping("/{quoteId}/bind")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Bind an accepted quote",
            description = "Binds an accepted quote and creates an immutable policy"
    )
    public Mono<PolicyResponse> bindQuote(
            @PathVariable UUID quoteId
    ) {
        return policyService.bindQuote(quoteId);
    }


    @PatchMapping("/{quoteId}/decline")
    @Operation(
            summary = "Decline quoted terms",
            description = """
                Customer declines the offered quote.
                Changes the status to DECLINED_BY_CUSTOMER.
                """
    )
    public Mono<QuoteResponse> declineQuote(
            @PathVariable UUID quoteId,
            @Valid @RequestBody DeclineQuoteRequest request
    ) {
        return quoteService.declineQuoteByCustomer(
                quoteId,
                request
        );
    }


    @PatchMapping("/{quoteId}/underwriter/reassign")
    @Operation(
            summary = "Manually reassign an underwriter",
            description = """
                Administrative exception operation.
                Normal quote submission performs automatic
                least-workload assignment.
                """
    )
    public Mono<QuoteResponse> reassignUnderwriter(
            @PathVariable UUID quoteId,
            @Valid @RequestBody
            AssignUnderwriterRequest request
    ) {
        return quoteService.reassignUnderwriter(
                quoteId,
                request
        );
    }


    @GetMapping("/{quoteId}")
    @Operation(summary = "Get quote by ID")
    public Mono<QuoteResponse> getQuoteById(
            @PathVariable UUID quoteId
    ) {
        return quoteService.getQuoteById(quoteId);
    }

    @GetMapping("/number/{quoteNumber}")
    @Operation(summary = "Get quote by quote number")
    public Mono<QuoteResponse> getQuoteByNumber(
            @PathVariable String quoteNumber
    ) {
        return quoteService.getQuoteByNumber(quoteNumber);
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all quotes belonging to a customer")
    public Flux<QuoteResponse> getQuotesByCustomerId(
            @PathVariable UUID customerId
    ) {
        return quoteService.getQuotesByCustomerId(customerId);
    }

    @GetMapping("/admin")
    @Operation(summary = "Get all quotes for administrators")
    public Flux<QuoteResponse> getAllQuotesForAdministration() {
        return quoteService.getAllQuotesForAdministration();
    }




}
