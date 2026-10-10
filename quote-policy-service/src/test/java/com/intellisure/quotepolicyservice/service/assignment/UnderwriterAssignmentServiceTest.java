package com.intellisure.quotepolicyservice.service.assignment;

import com.intellisure.quotepolicyservice.client.CustomerPartyClient;
import com.intellisure.quotepolicyservice.config.UnderwriterPoolProperties;
import com.intellisure.quotepolicyservice.repository.QuoteRepository;
import com.intellisure.quotepolicyservice.testsupport.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("UnderwriterAssignmentService")
class UnderwriterAssignmentServiceTest {

    private static final UUID FIRST =
            UUID.fromString("00000000-0000-0000-0000-00000000000a");

    private static final UUID SECOND =
            UUID.fromString("00000000-0000-0000-0000-00000000000b");

    private static final UUID THIRD =
            UUID.fromString("00000000-0000-0000-0000-00000000000c");

    private QuoteRepository quoteRepository;
    private CustomerPartyClient customerPartyClient;
    private UnderwriterPoolProperties properties;

    private UnderwriterAssignmentService service;

    @BeforeEach
    void setUp() {
        quoteRepository = mock(QuoteRepository.class);
        customerPartyClient = mock(CustomerPartyClient.class);
        properties = new UnderwriterPoolProperties();

        lenient().when(customerPartyClient.findAvailableEmployeesByRole(any()))
                .thenReturn(reactor.core.publisher.Flux.empty());

        service = new UnderwriterAssignmentService(
                quoteRepository,
                customerPartyClient,
                properties
        );
    }

    private void givenWorkload(UUID underwriterId, long activeQuotes) {
        lenient().when(
                quoteRepository.countActiveQuotesByUnderwriterId(
                        underwriterId
                )
        ).thenReturn(Mono.just(activeQuotes));
    }

    @Test
    @DisplayName("completes empty when no underwriter is configured")
    void emptyPool() {
        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .verifyComplete();

        verifyNoInteractions(quoteRepository);
    }

    @Test
    @DisplayName("completes empty when the configured pool is null")
    void nullPool() {
        properties.setEligibleUnderwriterIds(null);

        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .verifyComplete();
    }

    @Test
    @DisplayName("selects the underwriter with the fewest active quotes")
    void selectsLeastLoaded() {
        properties.setEligibleUnderwriterIds(
                List.of(FIRST, SECOND, THIRD)
        );

        givenWorkload(FIRST, 7L);
        givenWorkload(SECOND, 2L);
        givenWorkload(THIRD, 5L);

        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .expectNext(SECOND)
                .verifyComplete();
    }

    @Test
    @DisplayName("breaks ties deterministically on the underwriter id")
    void breaksTiesById() {
        properties.setEligibleUnderwriterIds(
                List.of(THIRD, FIRST, SECOND)
        );

        givenWorkload(FIRST, 1L);
        givenWorkload(SECOND, 1L);
        givenWorkload(THIRD, 1L);

        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .expectNext(FIRST)
                .verifyComplete();
    }

    @Test
    @DisplayName("treats a missing count as zero")
    void treatsMissingCountAsZero() {
        properties.setEligibleUnderwriterIds(
                List.of(FIRST, SECOND)
        );

        givenWorkload(FIRST, 4L);
        when(
                quoteRepository.countActiveQuotesByUnderwriterId(SECOND)
        ).thenReturn(Mono.empty());

        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .expectNext(SECOND)
                .verifyComplete();
    }

    @Test
    @DisplayName("selects the only configured underwriter")
    void singleUnderwriter() {
        properties.setEligibleUnderwriterIds(
                List.of(TestFixtures.UNDERWRITER_ID)
        );

        givenWorkload(TestFixtures.UNDERWRITER_ID, 0L);

        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .expectNext(TestFixtures.UNDERWRITER_ID)
                .verifyComplete();

        verify(quoteRepository).countActiveQuotesByUnderwriterId(
                TestFixtures.UNDERWRITER_ID
        );
    }

    @Test
    @DisplayName("propagates a repository failure")
    void propagatesRepositoryFailure() {
        properties.setEligibleUnderwriterIds(List.of(FIRST));

        when(
                quoteRepository.countActiveQuotesByUnderwriterId(FIRST)
        ).thenReturn(
                Mono.error(new IllegalStateException("db down"))
        );

        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    @DisplayName("the default pool configuration is empty")
    void defaultPoolIsEmpty() {
        assertEquals(
                0,
                new UnderwriterPoolProperties()
                        .getEligibleUnderwriterIds()
                        .size()
        );
    }

    @Test
    @DisplayName("an unknown underwriter is not selected")
    void ignoresUnreachableUnderwriters() {
        properties.setEligibleUnderwriterIds(List.of(FIRST));

        lenient().when(
                quoteRepository.countActiveQuotesByUnderwriterId(any())
        ).thenReturn(Mono.just(0L));

        StepVerifier.create(service.selectLeastLoadedUnderwriter())
                .expectNext(FIRST)
                .verifyComplete();
    }
}
