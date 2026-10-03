package com.intellisure.customerpartyservice.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final Sinks.Many<CustomerProfileChangedEvent> customerProfileChangedEventSink;

    public Mono<Void> publishCustomerProfileChanged(CustomerProfileChangedEvent event) {
        return Mono.fromRunnable(() -> {
            Sinks.EmitResult result = customerProfileChangedEventSink.tryEmitNext(event);
            if (result.isFailure()) {
                log.warn("Failed to emit CustomerProfileChangedEvent: {}", result);
            } else {
                log.debug("Emitted CustomerProfileChangedEvent: customerId={}, field={}, version={}",
                        event.customerId(), event.fieldName(), event.newVersion());
            }
        });
    }

    public Sinks.Many<CustomerProfileChangedEvent> getCustomerProfileChangedEventSink() {
        return customerProfileChangedEventSink;
    }
}