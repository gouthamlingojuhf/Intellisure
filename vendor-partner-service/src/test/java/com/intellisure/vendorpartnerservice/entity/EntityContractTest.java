package com.intellisure.vendorpartnerservice.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EntityContractTest {
    @Test
    void assignmentAndVendorEntitiesExposePersistenceAndMutationContracts() {
        UUID id = UUID.randomUUID(); LocalDateTime now = LocalDateTime.now();
        Vendor vendor = Vendor.builder().vendorId(id).legalName("Legal").displayName("Display").vendorType(VendorType.TOWING)
                .serviceTypes(List.of("TOWING")).capabilities(List.of("CAP")).serviceAreas(List.of("AREA"))
                .contactName("Name").contactPhone("Phone").contactEmail("Email").verificationStatus(VendorVerificationStatus.VERIFIED)
                .activeStatus(VendorActiveStatus.ACTIVE).createdAt(now).updatedAt(now).isNew(true).build();
        assertEquals(id, vendor.getId()); assertTrue(vendor.isNew());
        vendor.setVendorId(id); vendor.setLegalName("New Legal"); vendor.setDisplayName("New Display"); vendor.setVendorType(VendorType.PROPERTY_RESTORATION);
        vendor.setServiceTypes(List.of()); vendor.setCapabilities(List.of()); vendor.setServiceAreas(List.of()); vendor.setContactName("New");
        vendor.setContactPhone("New Phone"); vendor.setContactEmail("new@email"); vendor.setVerificationStatus(VendorVerificationStatus.REJECTED);
        vendor.setActiveStatus(VendorActiveStatus.INACTIVE); vendor.setCreatedAt(now); vendor.setUpdatedAt(now); vendor.setNew(false);
        assertFalse(vendor.isNew());

        VendorAssignment assignment = VendorAssignment.builder().assignmentId(id).vendorId(id).assignmentType(AssignmentType.RESTORATION)
                .claimId(id).recoveryCaseId(id).status(AssignmentStatus.DISPATCHED).taskDescription("Task").dueDate(LocalDate.now())
                .priority("HIGH").acceptedAt(now).completedAt(now).evidenceDocumentIds(List.of(id.toString())).createdAt(now).updatedAt(now).isNew(true).build();
        assertEquals(id, assignment.getId()); assertTrue(assignment.isNew());
        assignment.setAssignmentId(id); assignment.setVendorId(id); assignment.setAssignmentType(AssignmentType.RECOVERY_REPAIR); assignment.setClaimId(id);
        assignment.setRecoveryCaseId(id); assignment.setStatus(AssignmentStatus.COMPLETED); assignment.setTaskDescription("New task"); assignment.setDueDate(LocalDate.now());
        assignment.setPriority("LOW"); assignment.setAcceptedAt(now); assignment.setCompletedAt(now); assignment.setEvidenceDocumentIds(List.of());
        assignment.setCreatedAt(now); assignment.setUpdatedAt(now); assignment.setNew(false); assertFalse(assignment.isNew());
    }

    @Test
    void onboardingAndPerformanceEntitiesExposeAllFields() {
        UUID id = UUID.randomUUID(); LocalDateTime now = LocalDateTime.now();
        VendorOnboardingRequest onboarding = VendorOnboardingRequest.builder().onboardingRequestId(id).vendorId(id).status(OnboardingStatus.SUBMITTED)
                .submittedAt(now).reviewedAt(now).reviewerId(id).rejectionReason("reason").isNew(true).build();
        assertEquals(id, onboarding.getId()); assertTrue(onboarding.isNew());
        onboarding.setOnboardingRequestId(id); onboarding.setVendorId(id); onboarding.setStatus(OnboardingStatus.VERIFIED); onboarding.setSubmittedAt(now);
        onboarding.setReviewedAt(now); onboarding.setReviewerId(id); onboarding.setRejectionReason("new"); onboarding.setNew(false); assertFalse(onboarding.isNew());

        VendorPerformance performance = VendorPerformance.builder().performanceId(id).vendorId(id).assignmentId(id)
                .qualityScore(BigDecimal.ONE).timelinessScore(BigDecimal.ONE).communicationScore(BigDecimal.ONE).outcomeScore(BigDecimal.ONE)
                .overallScore(BigDecimal.ONE).note("note").recordedAt(now).isNew(true).build();
        assertEquals(id, performance.getId()); assertTrue(performance.isNew());
        performance.setPerformanceId(id); performance.setVendorId(id); performance.setAssignmentId(id); performance.setQualityScore(BigDecimal.TEN);
        performance.setTimelinessScore(BigDecimal.TEN); performance.setCommunicationScore(BigDecimal.TEN); performance.setOutcomeScore(BigDecimal.TEN);
        performance.setOverallScore(BigDecimal.TEN); performance.setNote("new"); performance.setRecordedAt(now); performance.setNew(false); assertFalse(performance.isNew());
    }
}
