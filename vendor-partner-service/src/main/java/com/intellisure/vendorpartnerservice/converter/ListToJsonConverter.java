package com.intellisure.vendorpartnerservice.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.util.List;

@WritingConverter
public class ListToJsonConverter implements Converter<List<String>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convert(List<String> source) {
        if (source == null) return "[]";
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
