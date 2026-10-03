package com.intellisure.quotepolicyservice.dto;

import java.util.UUID;

public record BindRenewalRequest(
        UUID boundByUserId
) {}