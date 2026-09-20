package com.intellisure.customerpartyservice.converter;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UuidConverterTest {

    @Test
    void convertsUuidToSixteenBytesAndBack() {
        UUID source = UUID.randomUUID();

        byte[] encoded = new UuidToBytesConverter().convert(source);
        UUID decoded = new BytesToUuidConverter().convert(encoded);

        assertEquals(16, encoded.length);
        assertEquals(source, decoded);
    }

    @Test
    void conversionUsesStableBigEndianUuidRepresentation() {
        UUID source = UUID.fromString("00112233-4455-6677-8899-aabbccddeeff");

        assertArrayEquals(
                new byte[] {0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
                        (byte) 0x88, (byte) 0x99, (byte) 0xaa, (byte) 0xbb,
                        (byte) 0xcc, (byte) 0xdd, (byte) 0xee, (byte) 0xff},
                new UuidToBytesConverter().convert(source));
    }
}
