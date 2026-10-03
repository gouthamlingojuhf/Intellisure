package com.intellisure.vendorpartnerservice.repository;

import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorActiveStatus;
import com.intellisure.vendorpartnerservice.entity.VendorType;
import com.intellisure.vendorpartnerservice.entity.VendorVerificationStatus;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VendorRepository extends R2dbcRepository<Vendor, UUID> {
    Flux<Vendor> findByVendorType(VendorType vendorType);
    Flux<Vendor> findByActiveStatus(VendorActiveStatus activeStatus);
    Flux<Vendor> findByVerificationStatus(VendorVerificationStatus verificationStatus);
    Flux<Vendor> findByVendorTypeAndActiveStatus(VendorType vendorType, VendorActiveStatus activeStatus);
    Flux<Vendor> findByServiceTypesContaining(String serviceType);
    Flux<Vendor> findByServiceAreasContaining(String serviceArea);
    Mono<Vendor> findByLegalName(String legalName);
}