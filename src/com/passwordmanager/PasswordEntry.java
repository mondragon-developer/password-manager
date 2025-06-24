package com.passwordmanager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a password entry containing a name, password, and metadata.
 * This class encapsulates password entry data and provides controlled access
 * to password information.
 * 
 * @author Jose Mondragon
 * @version 1.0
 * @since 2025-06-03
 */
public class PasswordEntry {
    
    /** The name or identifier for this password entry */
    private final String name;
    
    /** The generated password */
    private final String password;
    
    /** The timestamp when this entry was created */
    private final LocalDateTime createdAt;
    
    /** Flag indicating if special characters were used in password generation */
    private final boolean hasSpecialCharacters;
    
    /** Date formatter for consistent timestamp formatting */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructs a new PasswordEntry with the specified parameters.
     * 
     * @param name the name or identifier for this password entry
     * @param password the generated password
     * @param hasSpecialCharacters indicates if special characters were used
     * @throws IllegalArgumentException if name or password is null or empty
     */
    public PasswordEntry(String name, String password, boolean hasSpecialCharacters) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        this.name = name.trim();
        this.password = password;
        this.hasSpecialCharacters = hasSpecialCharacters;
        this.createdAt = LocalDateTime.now();
    }
    
    /**
     * Returns the name of this password entry.
     * 
     * @return the name of the password entry
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the password for this entry.
     * 
     * @return the password string
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Returns the creation timestamp of this entry.
     * 
     * @return the LocalDateTime when this entry was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Indicates whether this password was generated with special characters.
     * 
     * @return true if special characters were used, false otherwise
     */
    public boolean hasSpecialCharacters() {
        return hasSpecialCharacters;
    }
    
    /**
     * Returns the creation timestamp formatted as a string.
     * 
     * @return formatted timestamp string
     */
    public String getFormattedTimestamp() {
        return createdAt.format(FORMATTER);
    }
    
    /**
     * Returns the password length.
     * 
     * @return the length of the password
     */
    public int getPasswordLength() {
        return password.length();
    }
    
    /**
     * Converts this password entry to a formatted string suitable for file storage.
     * Format: "Name: [name] | Password: [password] | Created: [timestamp] | Special Chars: [yes/no]"
     * 
     * @return formatted string representation of this entry
     */
    public String toFileFormat() {
        return String.format("Name: %s | Password: %s | Created: %s | Special Chars: %s",
                name, password, getFormattedTimestamp(), hasSpecialCharacters ? "Yes" : "No");
    }
    
    /**
     * Returns a string representation of this password entry.
     * Note: For security reasons, the password is masked in this representation.
     * 
     * @return string representation with masked password
     */
    @Override
    public String toString() {
        return String.format("PasswordEntry{name='%s', passwordLength=%d, hasSpecialChars=%b, created=%s}",
                name, password.length(), hasSpecialCharacters, getFormattedTimestamp());
    }
    
    /**
     * Compares this password entry with another object for equality.
     * Two password entries are considered equal if they have the same name and password.
     * 
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PasswordEntry that = (PasswordEntry) obj;
        return name.equals(that.name) && password.equals(that.password);
    }
    
    /**
     * Returns the hash code for this password entry.
     * 
     * @return hash code based on name and password
     */
    @Override
    public int hashCode() {
        return name.hashCode() * 31 + password.hashCode();
    }
}