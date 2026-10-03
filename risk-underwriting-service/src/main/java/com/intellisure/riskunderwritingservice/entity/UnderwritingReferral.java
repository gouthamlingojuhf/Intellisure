package com.intellisure.riskunderwritingservice.entity;

import com.intellisure.riskunderwritingservice.enums.ReferralStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("underwriting_referral")
public class UnderwritingReferral {

    @Id
    @Column("referral_id")
    private UUID referralId;

    @Column("assessment_id")
    private UUID assessmentId;

    @Column("quote_id")
    private UUID quoteId;

    @Column("referral_reason")
    private String referralReason;

    @Column("required_authority_level")
    private String requiredAuthorityLevel;

    @Column("referred_by")
    private UUID referredBy;

    @Column("referred_to")
    private UUID referredTo;

    @Column("status")
    private ReferralStatus status;

    @Column("resolution_note")
    private String resolutionNote;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("resolved_at")
    private LocalDateTime resolvedAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}