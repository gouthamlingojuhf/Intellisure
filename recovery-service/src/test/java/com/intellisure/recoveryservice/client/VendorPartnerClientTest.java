package com.intellisure.recoveryservice.client;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VendorPartnerClientTest {
    @Test
    void createsNetworkAssignmentPayloadWithDefaults() {
        WebClient.Builder builder = mock(WebClient.Builder.class);
        WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(builder.baseUrl("http://vendor-partner-service")).thenReturn(builder); when(builder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(uriSpec); when(uriSpec.uri("/api/vendor-assignments")).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenReturn(headersSpec); when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("assignmentId", "a")));
        VendorPartnerClient client = new VendorPartnerClient(builder);
        UUID vendor = UUID.randomUUID(); UUID recovery = UUID.randomUUID();
        ArgumentCaptor<Object> body = ArgumentCaptor.forClass(Object.class);
        StepVerifier.create(client.createVendorAssignment(vendor, null, recovery, null, null))
                .assertNext(result -> assertEquals("a", result.get("assignmentId"))).verifyComplete();
        verify(bodySpec).bodyValue(body.capture());
        Map<?, ?> payload = (Map<?, ?>) body.getValue();
        assertEquals(vendor, payload.get("vendorId")); assertEquals(recovery, payload.get("recoveryCaseId"));
        assertEquals("NETWORK_VENDOR", payload.get("recoveryPath")); assertEquals("HIGH", payload.get("priority"));
        assertNotNull(payload.get("claimId")); assertNotNull(payload.get("dueDate"));
    }

    @Test
    void preservesExplicitAssignmentFieldsAndConvertsDownstreamErrorsToEmpty() {
        WebClient.Builder builder = mock(WebClient.Builder.class); WebClient webClient = mock(WebClient.class);
        WebClient.RequestBodyUriSpec uriSpec = mock(WebClient.RequestBodyUriSpec.class); WebClient.RequestBodySpec bodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec headersSpec = mock(WebClient.RequestHeadersSpec.class); WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        when(builder.baseUrl(anyString())).thenReturn(builder); when(builder.build()).thenReturn(webClient); when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec); when(bodySpec.bodyValue(any())).thenReturn(headersSpec); when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new IllegalStateException("unavailable")));
        VendorPartnerClient client = new VendorPartnerClient(builder);
        UUID vendor = UUID.randomUUID(); UUID claim = UUID.randomUUID(); UUID recovery = UUID.randomUUID(); LocalDate due = LocalDate.of(2026, 2, 3);
        StepVerifier.create(client.createVendorAssignment(vendor, claim, recovery, "Repair building", due)).verifyComplete();
    }
}
