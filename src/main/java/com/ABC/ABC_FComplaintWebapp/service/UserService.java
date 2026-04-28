package com.ABC.ABC_FComplaintWebapp.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ABC.ABC_FComplaintWebapp.model.User;
import com.ABC.ABC_FComplaintWebapp.repositories.UserRepository;

/**
 * SECURITY FIX_001: User Service with BCrypt Password Encoding/Verification
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
 */
@Service
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injected from SecurityConfig - BCryptPasswordEncoder(12)

    /**
     * SECURITY FIX_001 - PROOF #1: Password Encoding During Registration
     * 
     * This method demonstrates the ACTUAL BCrypt encoding that proves Fix_001:
     * 
     * 1. Receives plain text password from user
     * 2. Encodes using passwordEncoder.encode() → BCrypt with strength 12
     * 3. Sets ONLY the hash in user object (plain text never stored)
     * 4. Persists user with hashed password
     * 5. Plain text lost after this method exits
     * 
     * @param username Username for the new account
     * @param email Email address
     * @param plainPassword Plain text password (temporary - immediately encoded)
     * @return User entity with BCrypt-hashed password
     */
    public User registerUser(String username, String email, String plainPassword) {
        // Input validation
        if (plainPassword == null || plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Check if user already exists
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

     
        String bcryptHash = passwordEncoder.encode(plainPassword);
        // Input: "MyPassword123"
        // Output: "$2a$12$<22-char-salt><31-char-hash>"
        // Time: ~100-150ms (intentionally slow to prevent brute force)
        
        // Create user with ONLY the hash (plain text not stored)
        User newUser = new User(username, email, bcryptHash);
        newUser.setRole("USER");
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());


        User savedUser = userRepository.save(newUser);

        logger.info(() -> String.format(
            "SECURITY: User registered with BCrypt-hashed password. Username: %s, Hash format: %s",
            username,
            bcryptHash.substring(0, 20) + "..." // Log only hash prefix for verification
        ));

        return savedUser;
    }

   
    public User authenticateUser(String username, String plainPassword) {
        // Retrieve user from database
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            logger.warning(() -> String.format(
                "SECURITY: Login attempt for non-existent user: %s", username
            ));
            return null;
        }

        User user = userOpt.get();

        // Check if account is locked
        if (user.getAccountLocked()) {
            logger.warning(() -> String.format(
                "SECURITY: Login attempt on locked account: %s", username
            ));
            return null;
        }

        
        boolean passwordMatches = passwordEncoder.matches(plainPassword, user.getHashedPassword());
       

        if (!passwordMatches) {
            // Password mismatch - increment failed login attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountLocked(true);
                logger.warning(() -> String.format(
                    "SECURITY: Account locked due to failed login attempts: %s", username
                ));
            }

            userRepository.save(user);

            logger.warning(() -> String.format(
                "SECURITY: Failed login attempt. Username: %s, Attempts: %d",
                username, user.getFailedLoginAttempts()
            ));

            return null;
        }
        user.setLastLogin(LocalDateTime.now());
        user.setFailedLoginAttempts(0); // Reset failed attempts on successful login
        userRepository.save(user);

        logger.info(() -> String.format(
            "SECURITY: Successful authentication via BCrypt verification. Username: %s",
            username
        ));

        return user;
    }


     */
    public void changePassword(User user, String plainPassword) {
        if (plainPassword == null || plainPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        String newHash = passwordEncoder.encode(plainPassword);
        
        user.setHashedPassword(newHash);
        user.setPasswordLastChanged(LocalDateTime.now());
        user.setFailedLoginAttempts(0);

        userRepository.save(user);

        logger.info(() -> String.format(
            "SECURITY: Password changed for user: %s", user.getUsername()
        ));
    }

    /**
     * SECURITY FIX_001 - PROOF #4: Verify Password Strength
     * 
     * Additional security layer checks:
     */
    public User getUserById(java.util.UUID id) {
        return userRepository.findById(id).orElse(null);
    }
