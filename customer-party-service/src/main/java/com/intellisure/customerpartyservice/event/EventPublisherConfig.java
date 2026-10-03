package com.intellisure.customerpartyservice.event;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class EventPublisherConfig {

    @Bean
    public Sinks.Many<CustomerProfileChangedEvent> customerProfileChangedEventSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}