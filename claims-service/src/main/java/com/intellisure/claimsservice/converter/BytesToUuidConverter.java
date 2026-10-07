package com.intellisure.claimsservice.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

@ReadingConverter
public class BytesToUuidConverter implements Converter<byte[], UUID> {

    @Override
    public UUID convert(byte[] source) {
        if (source == null || source.length != 16) return null;
        ByteBuffer buffer = ByteBuffer.wrap(source);
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}
