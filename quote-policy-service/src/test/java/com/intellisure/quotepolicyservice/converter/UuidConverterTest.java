package com.intellisure.quotepolicyservice.converter;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UuidConverterTest {

    @Test
    void roundTripsUuidThroughDatabaseBytes() {
        UUID source = UUID.randomUUID();

        UUID decoded = new BytesToUuidConverter().convert(
                new UuidToBytesConverter().convert(source));

        assertEquals(source, decoded);
    }
}
