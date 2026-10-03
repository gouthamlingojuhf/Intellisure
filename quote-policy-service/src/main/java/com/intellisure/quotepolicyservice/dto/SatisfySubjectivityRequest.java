package com.intellisure.quotepolicyservice.dto;

import java.util.List;
import java.util.UUID;

public record SatisfySubjectivityRequest(
        UUID satisfiedByUserId,
        List<UUID> evidenceDocumentIds
) {}