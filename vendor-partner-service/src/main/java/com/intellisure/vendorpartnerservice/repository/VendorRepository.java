package com.intellisure.vendorpartnerservice.repository;

import com.intellisure.vendorpartnerservice.entity.Vendor;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface VendorRepository extends R2dbcRepository<Vendor, UUID> {
    Flux<Vendor> findByVendorType(String vendorType);
    Flux<Vendor> findByStatus(String status);
}
