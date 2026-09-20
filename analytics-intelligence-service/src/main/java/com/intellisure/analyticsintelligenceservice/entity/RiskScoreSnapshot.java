package com.intellisure.analyticsintelligenceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("risk_score_snapshot")
public class RiskScoreSnapshot implements Persistable<UUID> {

    @Id
    private UUID snapshotId;

    private UUID customerId;
    private BigDecimal riskScore;
    private String riskBand;
    private String keyFactors;
    private String modelVersion;
    
    private LocalDateTime generatedAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return snapshotId;
    }
}
