package com.intellisure.quotepolicyservice.mapper;
import com.intellisure.quotepolicyservice.dto.QuoteResponse;
import com.intellisure.quotepolicyservice.entity.Quote;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuoteMapper {

    QuoteResponse toQuoteResponse(Quote quote);
}