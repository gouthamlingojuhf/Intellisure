package com.intellisure.vendorpartnerservice.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@ReadingConverter
public class JsonToListConverter implements Converter<String, List<String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> convert(String source) {
        if (source == null || source.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(source, new TypeReference<List<String>>() {});
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
