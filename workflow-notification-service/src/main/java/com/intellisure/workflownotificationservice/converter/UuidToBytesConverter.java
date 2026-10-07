package com.intellisure.workflownotificationservice.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

@WritingConverter
public class UuidToBytesConverter implements Converter<UUID, byte[]> {

    @Override
    public byte[] convert(UUID source) {
        if (source == null) return null;
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(source.getMostSignificantBits());
        buffer.putLong(source.getLeastSignificantBits());
        return buffer.array();
    }
}
