package com.intellisure.riskunderwritingservice.testsupport;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation.ReactiveInsert;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EntityTemplateStubber {

    private EntityTemplateStubber() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> void stubInsert(
            R2dbcEntityTemplate entityTemplate,
            Class<T> entityType,
            Function<T, T> resultMapper
    ) {
        ReactiveInsert insertOperation = mock(ReactiveInsert.class);

        when(entityTemplate.insert(entityType)).thenReturn(insertOperation);

        when(insertOperation.using(any())).thenAnswer(
                invocation -> Mono.just((T) resultMapper.apply(invocation.getArgument(0)))
        );
    }

    public static <T> void stubInsert(
            R2dbcEntityTemplate entityTemplate,
            Class<T> entityType
    ) {
        stubInsert(entityTemplate, entityType, Function.identity());
    }
}
