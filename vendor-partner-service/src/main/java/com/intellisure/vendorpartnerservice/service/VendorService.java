package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.RegisterVendorRequest;
import com.intellisure.vendorpartnerservice.dto.VendorResponse;
import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.dto.AssignmentResponse;
import com.intellisure.vendorpartnerservice.repository.VendorAssignmentRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorAssignmentRepository assignmentRepository;

    public Flux<VendorResponse> getVendors() { return vendorRepository.findAll().map(this::mapToResponse); }

    public Mono<VendorResponse> updateStatus(UUID id, String status) {
        return vendorRepository.findById(id).flatMap(v -> {
            v.setStatus(status); v.setUpdatedAt(LocalDateTime.now()); v.setNew(false);
            return vendorRepository.save(v);
        }).map(this::mapToResponse);
    }

    public Mono<AssignmentResponse> assign(UUID vendorId, UUID claimId, String service) {
        LocalDateTime now = LocalDateTime.now();
        VendorAssignment a = VendorAssignment.builder().assignmentId(UUID.randomUUID()).vendorId(vendorId)
                .claimId(claimId).serviceRequested(service).status("ASSIGNED").assignedDate(now.toLocalDate())
                .createdAt(now).updatedAt(now).isNew(true).build();
        return assignmentRepository.save(a).map(this::mapAssignment);
    }

    public Mono<AssignmentResponse> updateAssignment(UUID id, String status) {
        return assignmentRepository.findById(id).flatMap(a -> {
            a.setStatus(status); a.setUpdatedAt(LocalDateTime.now());
            if ("COMPLETED".equals(status)) a.setCompletedDate(LocalDate.now());
            a.setNew(false); return assignmentRepository.save(a);
        }).map(this::mapAssignment);
    }

    private AssignmentResponse mapAssignment(VendorAssignment a) {
        return new AssignmentResponse(a.getAssignmentId(), a.getVendorId(), a.getClaimId(), a.getServiceRequested(),
                a.getStatus(), a.getAssignedDate(), a.getCompletedDate(), a.getCost(), a.getCreatedAt(), a.getUpdatedAt());
    }

    public Mono<VendorResponse> registerVendor(RegisterVendorRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        Vendor vendor = Vendor.builder()
                .vendorId(UUID.randomUUID())
                .vendorName(request.vendorName())
                .vendorType(request.vendorType())
                .status("PENDING_ONBOARDING")
                .contactEmail(request.contactEmail())
                .contactPhone(request.contactPhone())
                .serviceRegions(request.serviceRegions())
                .createdAt(now)
                .updatedAt(now)
                .isNew(true)
                .build();

        return vendorRepository.save(vendor)
                .map(this::mapToResponse);
    }

    private VendorResponse mapToResponse(Vendor vendor) {
        return new VendorResponse(
                vendor.getVendorId(),
                vendor.getVendorName(),
                vendor.getVendorType(),
                vendor.getStatus(),
                vendor.getContactEmail(),
                vendor.getContactPhone(),
                vendor.getServiceRegions(),
                vendor.getCreatedAt(),
                vendor.getUpdatedAt()
        );
    }
}
