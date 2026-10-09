package com.intellisure.documentauditservice.converter;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UuidConverterTest {
    @Test
    void roundTripsUuidThroughMySqlBinaryRepresentation() {
        UUID source = UUID.randomUUID();
        UuidToBytesConverter writer = new UuidToBytesConverter();
        BytesToUuidConverter reader = new BytesToUuidConverter();

        assertThat(reader.convert(writer.convert(source))).isEqualTo(source);
        assertThat(writer.convert(null)).isNull();
        assertThat(reader.convert(null)).isNull();
        assertThat(reader.convert(new byte[15])).isNull();
    }
}
