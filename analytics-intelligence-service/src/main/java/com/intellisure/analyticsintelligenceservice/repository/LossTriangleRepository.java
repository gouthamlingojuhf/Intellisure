package com.intellisure.analyticsintelligenceservice.repository;

import com.intellisure.analyticsintelligenceservice.entity.LossTriangle;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface LossTriangleRepository extends R2dbcRepository<LossTriangle, UUID> {
    Flux<LossTriangle> findByAccidentYearOrderByDevelopmentYear(Integer accidentYear);
    Flux<LossTriangle> findByDevelopmentYear(Integer developmentYear);
    Mono<LossTriangle> findByAccidentYearAndDevelopmentYear(Integer accidentYear, Integer developmentYear);
}