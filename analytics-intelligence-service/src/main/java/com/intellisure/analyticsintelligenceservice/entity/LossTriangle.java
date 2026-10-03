package com.intellisure.analyticsintelligenceservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("loss_triangle")
public class LossTriangle implements Persistable<UUID> {

    @Id
    private UUID triangleId;

    private Integer accidentYear;
    private Integer developmentYear;
    private BigDecimal cumulativeIncurredClaims;
    private BigDecimal cumulativePaidClaims;
    private BigDecimal caseReserves;
    private BigDecimal ibnrReserves;

    private LocalDateTime calculatedAt;

    @Transient
    private boolean isNew = true;

    public static LossTriangle builder() {
        return new LossTriangle();
    }

    public LossTriangle triangleId(UUID triangleId) { this.triangleId = triangleId; return this; }
    public LossTriangle accidentYear(Integer accidentYear) { this.accidentYear = accidentYear; return this; }
    public LossTriangle developmentYear(Integer developmentYear) { this.developmentYear = developmentYear; return this; }
    public LossTriangle cumulativeIncurredClaims(BigDecimal cumulativeIncurredClaims) { this.cumulativeIncurredClaims = cumulativeIncurredClaims; return this; }
    public LossTriangle cumulativePaidClaims(BigDecimal cumulativePaidClaims) { this.cumulativePaidClaims = cumulativePaidClaims; return this; }
    public LossTriangle caseReserves(BigDecimal caseReserves) { this.caseReserves = caseReserves; return this; }
    public LossTriangle ibnrReserves(BigDecimal ibnrReserves) { this.ibnrReserves = ibnrReserves; return this; }
    public LossTriangle calculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; return this; }
    public LossTriangle isNew(boolean isNew) { this.isNew = isNew; return this; }
    public LossTriangle build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return triangleId;
    }

    public UUID getTriangleId() { return triangleId; }
    public void setTriangleId(UUID triangleId) { this.triangleId = triangleId; }
    public Integer getAccidentYear() { return accidentYear; }
    public void setAccidentYear(Integer accidentYear) { this.accidentYear = accidentYear; }
    public Integer getDevelopmentYear() { return developmentYear; }
    public void setDevelopmentYear(Integer developmentYear) { this.developmentYear = developmentYear; }
    public BigDecimal getCumulativeIncurredClaims() { return cumulativeIncurredClaims; }
    public void setCumulativeIncurredClaims(BigDecimal cumulativeIncurredClaims) { this.cumulativeIncurredClaims = cumulativeIncurredClaims; }
    public BigDecimal getCumulativePaidClaims() { return cumulativePaidClaims; }
    public void setCumulativePaidClaims(BigDecimal cumulativePaidClaims) { this.cumulativePaidClaims = cumulativePaidClaims; }
    public BigDecimal getCaseReserves() { return caseReserves; }
    public void setCaseReserves(BigDecimal caseReserves) { this.caseReserves = caseReserves; }
    public BigDecimal getIbnrReserves() { return ibnrReserves; }
    public void setIbnrReserves(BigDecimal ibnrReserves) { this.ibnrReserves = ibnrReserves; }
    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}