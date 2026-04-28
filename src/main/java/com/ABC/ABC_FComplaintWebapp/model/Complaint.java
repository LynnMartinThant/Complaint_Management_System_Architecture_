package com.ABC.ABC_FComplaintWebapp.model;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_tenant_id", columnList = "tenant_id"), // **Implementation**: UUID PK instead of sequential Long  
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Complaint {
    
  @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    
     // Tenant isolation for strict multi-tenant boundary enforcement
    
    @Column(name = "tenant_id", nullable = false)
    @NotNull(message = "Tenant ID is required")
    private Integer tenantId;

    
     //Ownership validation for access control checks
    
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private Integer userId;

    @Column(nullable = false)
    @NotBlank(message = "User name is required")
    private String userName;

    @Column(nullable = false)
    @NotBlank(message = "Title is required")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Description is required")
    private String description;

    @Column(nullable = false)
    @NotBlank(message = "Category is required")
    private String category;

    
     // Complaint lifecycle state
  
    @Column(nullable = false)
    @Builder.Default
    private String status = "Pending";

   
   

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
/*
 * SECURITY FIX_003: UUID-based Resource Identification
 * Weakness ID: Wk_003
 * Fix ID: Fix_003 – Replaced Sequential IDs with UUIDs & Enforcement of Ownership Checks
 * STRIDE: Information Disclosure, Tampering
 * OWASP: A01 Broken Access Control
 * CWE: CWE-639, CWE-863
 * CIA: Confidentiality, Integrity
 * ASVS: V4 – Access Control
 * D3FEND: D3-AAC Attribute-Based Access Control
    
 */
