package com.intellisure.quotepolicyservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@ConfigurationProperties(prefix = "intellisure.underwriting")
public class UnderwriterPoolProperties {

    private List<UUID> eligibleUnderwriterIds =
            new ArrayList<>();

    public List<UUID> getEligibleUnderwriterIds() {
        return eligibleUnderwriterIds;
    }

    public void setEligibleUnderwriterIds(
            List<UUID> eligibleUnderwriterIds
    ) {
        this.eligibleUnderwriterIds =
                eligibleUnderwriterIds;
    }
}