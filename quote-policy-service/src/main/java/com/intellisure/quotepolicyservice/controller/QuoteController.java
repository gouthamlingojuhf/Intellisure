package com.intellisure.quotepolicyservice.controller;

import com.intellisure.quotepolicyservice.dto.CreateQuoteRequest;
import com.intellisure.quotepolicyservice.dto.QuoteResponse;
import com.intellisure.quotepolicyservice.service.QuoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<QuoteResponse> createQuote(
            @Valid @RequestBody CreateQuoteRequest request) {

        return quoteService.createQuote(request);
    }


    @GetMapping("/{quoteId}")
    public Mono<QuoteResponse> getQuote(
            @PathVariable UUID quoteId) {

        return quoteService.getQuote(quoteId);
    }

    @GetMapping
    public Flux<QuoteResponse> getQuotes(@RequestParam UUID customerId) {
        return quoteService.getQuotesByCustomer(customerId);
    }

    @PostMapping("/{quoteId}/submit")
    public Mono<QuoteResponse> submit(@PathVariable UUID quoteId) {
        return quoteService.updateStatus(quoteId, "SUBMITTED");
    }

    @PostMapping("/{quoteId}/approve")
    public Mono<QuoteResponse> approve(@PathVariable UUID quoteId) {
        return quoteService.updateStatus(quoteId, "ACCEPTED");
    }

    @PostMapping("/{quoteId}/reject")
    public Mono<QuoteResponse> reject(@PathVariable UUID quoteId) {
        return quoteService.updateStatus(quoteId, "REJECTED");
    }


    @GetMapping("/customer/{customerId}")
    public Flux<QuoteResponse> getCustomerQuotes(
            @PathVariable UUID customerId) {

        return quoteService.getQuotesByCustomer(
                customerId
        );
    }
}