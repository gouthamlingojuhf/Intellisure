package com.intellisure.quotepolicyservice.config;

import com.intellisure.quotepolicyservice.converter.StringToUuidConverter;
import com.intellisure.quotepolicyservice.converter.UuidToStringConverter;
import com.intellisure.quotepolicyservice.entity.Endorsement;
import com.intellisure.quotepolicyservice.entity.EndorsementCoverage;
import com.intellisure.quotepolicyservice.entity.PremiumAudit;
import com.intellisure.quotepolicyservice.entity.QuoteVersion;
import com.intellisure.quotepolicyservice.entity.RenewalTransaction;
import com.intellisure.quotepolicyservice.entity.Subjectivity;
import com.intellisure.quotepolicyservice.entity.Policy;
import com.intellisure.quotepolicyservice.entity.PolicyCoverage;
import com.intellisure.quotepolicyservice.entity.Quote;
import com.intellisure.quotepolicyservice.entity.QuoteCoverage;
import com.intellisure.quotepolicyservice.entity.UnderwritingDecision;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.mapping.RelationalPersistentEntity;

import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.intellisure.quotepolicyservice.repository")
public class R2dbcConfig {

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(
            ConnectionFactory connectionFactory
    ) {
        R2dbcDialect dialect =
                DialectResolver.getDialect(connectionFactory);

        return R2dbcCustomConversions.of(
                dialect,
                List.of(
                        new UuidToStringConverter(),
                        new StringToUuidConverter()
                )
        );
    }

    @Bean
    public R2dbcMappingContext r2dbcMappingContext(R2dbcCustomConversions r2dbcCustomConversions) {
        R2dbcMappingContext context = new R2dbcMappingContext();
        context.setSimpleTypeHolder(r2dbcCustomConversions.getSimpleTypeHolder());
        return context;
    }
}