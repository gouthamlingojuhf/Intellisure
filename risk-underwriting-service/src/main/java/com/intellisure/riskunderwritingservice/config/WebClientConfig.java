package com.intellisure.riskunderwritingservice.config;

import com.intellisure.riskunderwritingservice.logging.CorrelationConstants;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder()
                .filter(correlationIdPropagationFilter());
    }

    private ExchangeFilterFunction correlationIdPropagationFilter() {
        return (request, next) -> Mono.deferContextual(context -> {
            String correlationId = context.getOrDefault(
                    CorrelationConstants.CONTEXT_KEY,
                    UUID.randomUUID().toString()
            );

            ClientRequest updatedRequest = ClientRequest.from(request)
                    .header(CorrelationConstants.HEADER, correlationId)
                    .build();

            return next.exchange(updatedRequest);
        });
    }
}
