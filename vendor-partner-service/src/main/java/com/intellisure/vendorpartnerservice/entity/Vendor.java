package com.intellisure.vendorpartnerservice.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Table("vendor")
public class Vendor implements Persistable<UUID> {

    @Id
    private UUID vendorId;

    private String legalName;
    private String displayName;
    private VendorType vendorType;
    
    @Column("service_types")
    private List<String> serviceTypes;
    
    @Column("capabilities")
    private List<String> capabilities;
    
    @Column("service_areas")
    private List<String> serviceAreas;
    
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    
    private VendorVerificationStatus verificationStatus;
    private VendorActiveStatus activeStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Transient
    private boolean isNew = true;

    public static Vendor builder() {
        return new Vendor();
    }
    
    public Vendor vendorId(UUID vendorId) { this.vendorId = vendorId; return this; }
    public Vendor legalName(String legalName) { this.legalName = legalName; return this; }
    public Vendor displayName(String displayName) { this.displayName = displayName; return this; }
    public Vendor vendorType(VendorType vendorType) { this.vendorType = vendorType; return this; }
    public Vendor serviceTypes(List<String> serviceTypes) { this.serviceTypes = serviceTypes; return this; }
    public Vendor capabilities(List<String> capabilities) { this.capabilities = capabilities; return this; }
    public Vendor serviceAreas(List<String> serviceAreas) { this.serviceAreas = serviceAreas; return this; }
    public Vendor contactName(String contactName) { this.contactName = contactName; return this; }
    public Vendor contactPhone(String contactPhone) { this.contactPhone = contactPhone; return this; }
    public Vendor contactEmail(String contactEmail) { this.contactEmail = contactEmail; return this; }
    public Vendor verificationStatus(VendorVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; return this; }
    public Vendor activeStatus(VendorActiveStatus activeStatus) { this.activeStatus = activeStatus; return this; }
    public Vendor createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
    public Vendor updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
    public Vendor isNew(boolean isNew) { this.isNew = isNew; return this; }
    
    public Vendor build() { return this; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return vendorId;
    }

    public UUID getVendorId() { return vendorId; }
    public void setVendorId(UUID vendorId) { this.vendorId = vendorId; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public VendorType getVendorType() { return vendorType; }
    public void setVendorType(VendorType vendorType) { this.vendorType = vendorType; }
    public List<String> getServiceTypes() { return serviceTypes; }
    public void setServiceTypes(List<String> serviceTypes) { this.serviceTypes = serviceTypes; }
    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }
    public List<String> getServiceAreas() { return serviceAreas; }
    public void setServiceAreas(List<String> serviceAreas) { this.serviceAreas = serviceAreas; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public VendorVerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VendorVerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public VendorActiveStatus getActiveStatus() { return activeStatus; }
    public void setActiveStatus(VendorActiveStatus activeStatus) { this.activeStatus = activeStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setNew(boolean isNew) { this.isNew = isNew; }
}