package com.intellisure.vendorpartnerservice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Table("vendor_onboarding_request")
public class VendorOnboardingRequest implements Persistable<UUID> {

    @Id
    @Column("onboarding_request_id")
    private UUID onboardingRequestId;

    @Column("vendor_id")
    private UUID vendorId;

    private OnboardingStatus status;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private UUID reviewerId;
    private String rejectionReason;

    @Transient
    private boolean isNew = true;

    public static VendorOnboardingRequest builder() {
        return new VendorOnboardingRequest();
    }
    
    public VendorOnboardingRequest onboardingRequestId(UUID onboardingRequestId) { this.onboardingRequestId = onboardingRequestId; return this; }
    public VendorOnboardingRequest vendorId(UUID vendorId) { this.vendorId = vendorId; return this; }
    public VendorOnboardingRequest status(OnboardingStatus status) { this.status = status; return this; }
    public VendorOnboardingRequest submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }
    public VendorOnboardingRequest reviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; return this; }
    public VendorOnboardingRequest reviewerId(UUID reviewerId) { this.reviewerId = reviewerId; return this; }
    public VendorOnboardingRequest rejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; return this; }
    public VendorOnboardingRequest isNew(boolean isNew) { this.isNew = isNew; return this; }
    
    public VendorOnboardingRequest build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return onboardingRequestId;
    }

    public UUID getOnboardingRequestId() { return onboardingRequestId; }
    public void setOnboardingRequestId(UUID onboardingRequestId) { this.onboardingRequestId = onboardingRequestId; }
    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
    public OnboardingStatus getStatus() { return status; }
    public void setStatus(OnboardingStatus status) { this.status = status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public UUID getReviewerId() { return reviewerId; }
    public void setReviewerId(UUID reviewerId) { this.reviewerId = reviewerId; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}