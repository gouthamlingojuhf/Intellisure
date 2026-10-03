package com.intellisure.quotepolicyservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CircuitBreakerEventConfig {

    @Bean
    public Customizer<
            ReactiveResilience4JCircuitBreakerFactory
            > circuitBreakerEventCustomizer() {

        return factory ->
                factory.addCircuitBreakerCustomizer(
                        this::registerEventLogging,
                        "riskUnderwritingService"
                );
    }

    private void registerEventLogging(
            CircuitBreaker circuitBreaker
    ) {
        circuitBreaker
                .getEventPublisher()

                .onStateTransition(event ->
                        log.warn(
                                "Circuit breaker state changed: "
                                        + "name={}, transition={}",
                                circuitBreaker.getName(),
                                event.getStateTransition()
                        )
                )

                .onError(event ->
                        log.warn(
                                "Circuit breaker recorded failure: "
                                        + "name={}, errorType={}, "
                                        + "durationMs={}",
                                circuitBreaker.getName(),
                                event.getThrowable()
                                        .getClass()
                                        .getSimpleName(),
                                event.getElapsedDuration()
                                        .toMillis()
                        )
                )

                .onSuccess(event ->
                        log.debug(
                                "Circuit breaker recorded success: "
                                        + "name={}, durationMs={}",
                                circuitBreaker.getName(),
                                event.getElapsedDuration()
                                        .toMillis()
                        )
                );
    }
}