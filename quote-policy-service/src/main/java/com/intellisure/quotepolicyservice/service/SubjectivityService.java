package com.intellisure.quotepolicyservice.service;

import com.intellisure.quotepolicyservice.dto.SubjectivityResponse;
import com.intellisure.quotepolicyservice.dto.AddSubjectivityRequest;
import com.intellisure.quotepolicyservice.dto.SatisfySubjectivityRequest;
import com.intellisure.quotepolicyservice.dto.WaiveSubjectivityRequest;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.Subjectivity;
import com.intellisure.quotepolicyservice.enums.QuoteStatus;
import com.intellisure.quotepolicyservice.enums.SubjectivityStatus;
import com.intellisure.quotepolicyservice.exception.AccessDeniedBusinessException;
import com.intellisure.quotepolicyservice.exception.BusinessException;
import com.intellisure.quotepolicyservice.exception.ResourceNotFoundException;
import com.intellisure.quotepolicyservice.mapper.SubjectivityMapper;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.repository.SubjectivityRepository;
import com.intellisure.quotepolicyservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubjectivityService {

    private final SubjectivityRepository subjectivityRepository;
    private final QuoteRepository quoteRepository;
    private final SubjectivityMapper subjectivityMapper;
    private final SecurityActorService securityActorService;

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<SubjectivityResponse> addSubjectivity(
            UUID quoteId,
            AddSubjectivityRequest request) {
        return quoteRepository.findById(quoteId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Quote not found: " + quoteId)))
                .flatMap(quote -> validateQuoteForSubjectivity(quote))
                .flatMap(quote -> securityActorService.currentUserId()
                        .flatMap(userId -> createSubjectivity(quote, request, userId)))
                .flatMap(this::buildSubjectivityResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<SubjectivityResponse> satisfySubjectivity(
            UUID subjectivityId,
            SatisfySubjectivityRequest request) {
        return subjectivityRepository.findById(subjectivityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Subjectivity not found: " + subjectivityId)))
                .flatMap(subjectivity -> validateSubjectivityForAction(subjectivity, SubjectivityStatus.OPEN))
                .flatMap(subjectivity -> securityActorService.currentUserId()
                        .flatMap(currentUserId -> {
                            if (!currentUserId.equals(request.satisfiedByUserId())) {
                                return Mono.error(new AccessDeniedBusinessException("User mismatch"));
                            }
                            return Mono.just(subjectivity);
                        }))
                .flatMap(subjectivity -> {
                    subjectivity.setStatus(SubjectivityStatus.SATISFIED);
                    subjectivity.setSatisfiedByUserId(request.satisfiedByUserId());
                    subjectivity.setSatisfiedAt(LocalDateTime.now());
                    subjectivity.setEvidenceDocumentIds(request.evidenceDocumentIds().toString());
                    subjectivity.setUpdatedAt(LocalDateTime.now());
                    return subjectivityRepository.save(subjectivity);
                })
                .flatMap(this::buildSubjectivityResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR')")
    @Transactional
    public Mono<SubjectivityResponse> waiveSubjectivity(
            UUID subjectivityId,
            WaiveSubjectivityRequest request) {
        return subjectivityRepository.findById(subjectivityId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Subjectivity not found: " + subjectivityId)))
                .flatMap(subjectivity -> validateSubjectivityForAction(subjectivity, SubjectivityStatus.OPEN))
                .flatMap(subjectivity -> securityActorService.currentUserId()
                        .flatMap(currentUserId -> {
                            if (!currentUserId.equals(request.waivedByUserId())) {
                                return Mono.error(new AccessDeniedBusinessException("User mismatch"));
                            }
                            return Mono.just(subjectivity);
                        }))
                .flatMap(subjectivity -> {
                    subjectivity.setStatus(SubjectivityStatus.WAIVED);
                    subjectivity.setSatisfiedByUserId(request.waivedByUserId());
                    subjectivity.setSatisfiedAt(LocalDateTime.now());
                    subjectivity.setUpdatedAt(LocalDateTime.now());
                    return subjectivityRepository.save(subjectivity);
                })
                .flatMap(this::buildSubjectivityResponse);
    }

    @PreAuthorize("hasAnyRole('UNDERWRITER', 'SYSTEM_ADMINISTRATOR', 'POLICYHOLDER')")
    public Flux<SubjectivityResponse> getSubjectivities(UUID quoteId) {
        return quoteRepository.findById(quoteId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Quote not found: " + quoteId)))
                .thenMany(subjectivityRepository.findAllByQuoteId(quoteId)
                        .flatMap(this::buildSubjectivityResponse));
    }

    private Mono<Quote> validateQuoteForSubjectivity(Quote quote) {
        if (quote.getStatus() != QuoteStatus.IN_REVIEW
                && quote.getStatus() != QuoteStatus.NEEDS_INFORMATION
                && quote.getStatus() != QuoteStatus.RISK_ASSESSMENT) {
            return Mono.error(new BusinessException("Subjectivities can only be added to quotes in IN_REVIEW, NEEDS_INFORMATION, or RISK_ASSESSMENT status. Current: " + quote.getStatus()));
        }
        return Mono.just(quote);
    }

    private Mono<Subjectivity> validateSubjectivityForAction(Subjectivity subjectivity, SubjectivityStatus requiredStatus) {
        if (subjectivity.getStatus() != requiredStatus) {
            return Mono.error(new BusinessException("Subjectivity must be in " + requiredStatus + " status for this action. Current: " + subjectivity.getStatus()));
        }
        return Mono.just(subjectivity);
    }

    private Mono<Subjectivity> createSubjectivity(Quote quote, AddSubjectivityRequest request, UUID userId) {
        LocalDateTime now = LocalDateTime.now();
        Subjectivity subjectivity = new Subjectivity(
                UUID.randomUUID(),
                quote.getQuoteId(),
                request.subjectivityCode(),
                request.description(),
                SubjectivityStatus.OPEN,
                null, null, "[]",
                now, now
        );

        return subjectivityRepository.save(subjectivity);
    }

    private Mono<SubjectivityResponse> buildSubjectivityResponse(Subjectivity subjectivity) {
        return Mono.just(subjectivityMapper.toResponse(subjectivity));
    }
}