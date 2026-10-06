package com.hotelmanagement.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords securely using BCrypt with salt.
 */
public class PasswordUtil {

    private static final int LOG_ROUNDS = 10;

    /**
     * Hashes a plaintext password using BCrypt.
     *
     * @param plainPassword plaintext string
     * @return String hashed password
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param plainPassword plaintext candidate
     * @param hashedPassword stored hash
     * @return boolean true if matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.trim().isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
