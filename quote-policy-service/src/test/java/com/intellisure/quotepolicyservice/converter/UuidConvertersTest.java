package com.intellisure.quotepolicyservice.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("R2DBC UUID converters")
class UuidConvertersTest {

    private final StringToUuidConverter stringToUuid =
            new StringToUuidConverter();

    private final UuidToStringConverter uuidToString =
            new UuidToStringConverter();

    @Test
    @DisplayName("reads a UUID from its canonical string form")
    void readsUuid() {
        UUID expected = UUID.randomUUID();

        assertEquals(
                expected,
                stringToUuid.convert(expected.toString())
        );
    }

    @Test
    @DisplayName("rejects a malformed UUID string")
    void rejectsMalformedUuid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> stringToUuid.convert("not-a-uuid")
        );
    }

    @Test
    @DisplayName("writes a UUID in its canonical string form")
    void writesUuid() {
        UUID value = UUID.fromString(
                "11111111-2222-3333-4444-555555555555"
        );

        assertEquals(
                "11111111-2222-3333-4444-555555555555",
                uuidToString.convert(value)
        );
    }

    @Test
    @DisplayName("round trips a UUID without loss")
    void roundTrips() {
        UUID original = UUID.randomUUID();

        assertEquals(
                original,
                stringToUuid.convert(uuidToString.convert(original))
        );
    }
}
