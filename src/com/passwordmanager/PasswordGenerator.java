package com.passwordmanager;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates cryptographically secure passwords with configurable options.
 * This class provides methods to generate strong passwords with or without
 * special characters, ensuring high entropy and security.
 * 
 * @author Jose Mondragon
 * @version 1.0
 * @since 2025-06-03
 */
public class PasswordGenerator {
    
    /** Lowercase letters used in password generation */
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    
    /** Uppercase letters used in password generation */
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /** Numeric digits used in password generation */
    private static final String DIGITS = "0123456789";
    
    /** Special characters used in password generation */
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    /** Default minimum password length */
    private static final int DEFAULT_MIN_LENGTH = 8;
    
    /** Default maximum password length */
    private static final int DEFAULT_MAX_LENGTH = 16;
    
    /** Cryptographically secure random number generator */
    private final SecureRandom secureRandom;
    
    /** Minimum password length */
    private int minLength;
    
    /** Maximum password length */
    private int maxLength;
    
    /**
     * Constructs a new PasswordGenerator with default length settings.
     * Default range: 8-16 characters.
     */
    public PasswordGenerator() {
        this(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);
    }
    
    /**
     * Constructs a new PasswordGenerator with specified length range.
     * 
     * @param minLength minimum password length (must be at least 4)
     * @param maxLength maximum password length (must be greater than minLength)
     * @throws IllegalArgumentException if length parameters are invalid
     */
    public PasswordGenerator(int minLength, int maxLength) {
        if (minLength < 4) {
            throw new IllegalArgumentException("Minimum length must be at least 4 characters");
        }
        if (maxLength < minLength) {
            throw new IllegalArgumentException("Maximum length must be greater than minimum length");
        }
        if (maxLength > 128) {
            throw new IllegalArgumentException("Maximum length cannot exceed 128 characters");
        }
        
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Generates a strong password without special characters.
     * The password will contain uppercase letters, lowercase letters, and digits.
     * 
     * @return a randomly generated password without special characters
     */
    public String generatePassword() {
        return generatePassword(false);
    }
    
    /**
     * Generates a strong password with optional special characters.
     * The password will always contain at least one character from each
     * included character set to ensure complexity.
     * 
     * @param includeSpecialChars if true, includes special characters in the password
     * @return a randomly generated password
     */
    public String generatePassword(boolean includeSpecialChars) {
        int length = generateRandomLength();
        return generatePassword(length, includeSpecialChars);
    }
    
    /**
     * Generates a password with specified length and character options.
     * 
     * @param length the desired password length
     * @param includeSpecialChars if true, includes special characters
     * @return a randomly generated password of the specified length
     * @throws IllegalArgumentException if length is outside valid range
     */
    public String generatePassword(int length, boolean includeSpecialChars) {
        if (length < 4 || length > 128) {
            throw new IllegalArgumentException("Password length must be between 4 and 128 characters");
        }
        
        String characterSet = buildCharacterSet(includeSpecialChars);
        List<Character> passwordChars = new ArrayList<>();
        
        // Ensure at least one character from each character type
        ensureCharacterComplexity(passwordChars, includeSpecialChars);
        
        // Fill remaining positions with random characters
        int remainingLength = length - passwordChars.size();
        for (int i = 0; i < remainingLength; i++) {
            char randomChar = characterSet.charAt(secureRandom.nextInt(characterSet.length()));
            passwordChars.add(randomChar);
        }
        
        // Shuffle the password to avoid predictable patterns
        Collections.shuffle(passwordChars, secureRandom);
        
        // Convert to string
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }
        
        return password.toString();
    }
    
    /**
     * Validates password strength and returns a score from 0-100.
     * Higher scores indicate stronger passwords.
     * 
     * @param password the password to validate
     * @return strength score (0-100)
     */
    public int validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length scoring (up to 30 points)
        if (password.length() >= 8) score += 10;
        if (password.length() >= 12) score += 10;
        if (password.length() >= 16) score += 10;
        
        // Character variety scoring (up to 70 points)
        if (containsCharacterType(password, LOWERCASE)) score += 15;
        if (containsCharacterType(password, UPPERCASE)) score += 15;
        if (containsCharacterType(password, DIGITS)) score += 15;
        if (containsCharacterType(password, SPECIAL_CHARACTERS)) score += 25;
        
        return Math.min(score, 100);
    }
    
    /**
     * Sets the minimum password length.
     * 
     * @param minLength the new minimum length (must be at least 4)
     * @throws IllegalArgumentException if minLength is invalid
     */
    public void setMinLength(int minLength) {
        if (minLength < 4) {
            throw new IllegalArgumentException("Minimum length must be at least 4 characters");
        }
        if (minLength > this.maxLength) {
            throw new IllegalArgumentException("Minimum length cannot exceed maximum length");
        }
        this.minLength = minLength;
    }
    
    /**
     * Sets the maximum password length.
     * 
     * @param maxLength the new maximum length
     * @throws IllegalArgumentException if maxLength is invalid
     */
    public void setMaxLength(int maxLength) {
        if (maxLength < this.minLength) {
            throw new IllegalArgumentException("Maximum length cannot be less than minimum length");
        }
        if (maxLength > 128) {
            throw new IllegalArgumentException("Maximum length cannot exceed 128 characters");
        }
        this.maxLength = maxLength;
    }
    
    /**
     * Returns the current minimum password length.
     * 
     * @return the minimum password length
     */
    public int getMinLength() {
        return minLength;
    }
    
    /**
     * Returns the current maximum password length.
     * 
     * @return the maximum password length
     */
    public int getMaxLength() {
        return maxLength;
    }
    
    /**
     * Builds the character set based on inclusion options.
     * 
     * @param includeSpecialChars whether to include special characters
     * @return the complete character set for password generation
     */
    private String buildCharacterSet(boolean includeSpecialChars) {
        StringBuilder charSet = new StringBuilder();
        charSet.append(LOWERCASE);
        charSet.append(UPPERCASE);
        charSet.append(DIGITS);
        
        if (includeSpecialChars) {
            charSet.append(SPECIAL_CHARACTERS);
        }
        
        return charSet.toString();
    }
    
    /**
     * Ensures password complexity by adding at least one character from each type.
     * 
     * @param passwordChars the list to add characters to
     * @param includeSpecialChars whether special characters should be included
     */
    private void ensureCharacterComplexity(List<Character> passwordChars, boolean includeSpecialChars) {
        // Add at least one lowercase letter
        passwordChars.add(LOWERCASE.charAt(secureRandom.nextInt(LOWERCASE.length())));
        
        // Add at least one uppercase letter
        passwordChars.add(UPPERCASE.charAt(secureRandom.nextInt(UPPERCASE.length())));
        
        // Add at least one digit
        passwordChars.add(DIGITS.charAt(secureRandom.nextInt(DIGITS.length())));
        
        // Add at least one special character if requested
        if (includeSpecialChars) {
            passwordChars.add(SPECIAL_CHARACTERS.charAt(secureRandom.nextInt(SPECIAL_CHARACTERS.length())));
        }
    }
    
    /**
     * Generates a random length within the configured range.
     * 
     * @return a random length between minLength and maxLength (inclusive)
     */
    private int generateRandomLength() {
        return secureRandom.nextInt(maxLength - minLength + 1) + minLength;
    }
    
    /**
     * Checks if a password contains at least one character from the specified character set.
     * 
     * @param password the password to check
     * @param characterSet the character set to search for
     * @return true if at least one character from the set is found
     */
    private boolean containsCharacterType(String password, String characterSet) {
        for (char c : password.toCharArray()) {
            if (characterSet.indexOf(c) != -1) {
                return true;
            }
        }
        return false;
    }
}