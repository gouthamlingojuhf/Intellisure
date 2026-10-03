package com.intellisure.quotepolicyservice.service.assignment;

import java.util.UUID;

public record UnderwriterWorkload(

        UUID underwriterId,

        long activeQuoteCount
) {
}