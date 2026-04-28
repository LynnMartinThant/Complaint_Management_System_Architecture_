package com.ABC.ABC_FComplaintWebapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * SECURITY FIX_001: User Model with Secure Password Hashing
 *
 * Weakness ID: Wk_001
 * Fix ID: Fix_001 – Secure Password Hashing using BCrypt
 * STRIDE: Spoofing (Primary), Information Disclosure (Secondary)
 * OWASP: A02 – Cryptographic Failures
 * CWE: CWE-522 – Insufficiently Protected Credentials
 * CIA: Confidentiality, Integrity
 * ASVS: V6.3, V6.7 – Authentication Security
 * D3FEND: D3-PH Password Hashing
 *
 * Implementation Details:
 * - Hash format: BCrypt
 * - Plain text passwords are NEVER stored
 * - Password verification uses BCrypt matches()
 * - Adaptive work factor improves brute-force resistance
 *
 * Proof of Implementation:
 * 1. SecurityConfig.java → BCryptPasswordEncoder bean
 * 2. UserService.java → registerUser() uses encoder.encode()
 * 3. UserService.java → authenticateUser() uses encoder.matches()
 */

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_username", columnList = "username", unique = true)
    }
)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "hashedPassword") // SECURITY: prevents password hash exposure in logs

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Username is required")
    private String username;

    @Column(nullable = false, unique = true)
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

   
    @Column(nullable = false, length = 70) 
    @NotBlank(message = "Password is required")
    private String hashedPassword; // hashed password only

    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * SECURITY FIX_002:
     * Failed login tracking for brute-force protection
     */
    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * SECURITY FIX_002:
     * Account lock after repeated failed authentication attempts
     */
    @Column(name = "account_locked", nullable = false) 
    @Builder.Default
    private Boolean accountLocked = false;



    /**
     * SECURITY FIX_001:
     * Track password change for security auditing
     */
    @Column(name = "password_last_changed")
    @Builder.Default
    private LocalDateTime passwordLastChanged = LocalDateTime.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
