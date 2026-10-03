package com.intellisure.riskunderwritingservice.config;

import com.intellisure.riskunderwritingservice.converter.StringToUuidConverter;
import com.intellisure.riskunderwritingservice.converter.UuidToStringConverter;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;

import java.util.List;

@Configuration
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(
            ConnectionFactory connectionFactory
    ) {
        R2dbcDialect dialect =
                DialectResolver.getDialect(
                        connectionFactory
                );

        return R2dbcCustomConversions.of(
                dialect,
                List.of(
                        new UuidToStringConverter(),
                        new StringToUuidConverter()
                )
        );
    }
}