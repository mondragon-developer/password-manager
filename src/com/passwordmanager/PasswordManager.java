package com.passwordmanager;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages password storage and retrieval operations for the password manager application.
 * This class handles file I/O operations, ensuring data persistence and providing
 * methods to save, load, and manage password entries.
 * 
 * @author Jose Mondragon
 * @version 1.0
 * @since 2025-06-03
 */
public class PasswordManager {
    
    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(PasswordManager.class.getName());
    
    /** Default filename for password storage */
    private static final String DEFAULT_FILENAME = "passwords.txt";
    
    /** Directory for storing password files */
    private static final String STORAGE_DIRECTORY = "password_storage";
    
    /** File separator for consistent formatting */
    private static final String SEPARATOR = "----------------------------------------";
    
    /** The file path where passwords are stored */
    private final Path filePath;
    
    /** List of password entries currently managed */
    private final List<PasswordEntry> passwords;
    
    /** Date formatter for file headers */
    private static final DateTimeFormatter HEADER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Constructs a new PasswordManager with the default filename.
     * Creates the storage directory if it doesn't exist.
     * 
     * @throws PasswordManagerException if initialization fails
     */
    public PasswordManager() throws PasswordManagerException {
        this(DEFAULT_FILENAME);
    }
    
    /**
     * Constructs a new PasswordManager with a specified filename.
     * Creates the storage directory if it doesn't exist.
     * 
     * @param filename the name of the file to store passwords
     * @throws PasswordManagerException if initialization fails
     */
    public PasswordManager(String filename) throws PasswordManagerException {
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }
        
        this.passwords = new ArrayList<>();
        
        try {
            // Create storage directory if it doesn't exist
            Path storageDir = Paths.get(STORAGE_DIRECTORY);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
                LOGGER.info("Created storage directory: " + storageDir.toAbsolutePath());
            }
            
            this.filePath = storageDir.resolve(filename.trim());
            
            // Load existing passwords if file exists
            if (Files.exists(filePath)) {
                loadPasswordsFromFile();
            }
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize PasswordManager", e);
            throw new PasswordManagerException("Failed to initialize password manager: " + e.getMessage(), e);
        }
    }
    
    /**
     * Saves a password entry to the file and adds it to the in-memory collection.
     * 
     * @param entry the password entry to save
     * @throws PasswordManagerException if saving fails
     * @throws IllegalArgumentException if entry is null
     */
    public synchronized void savePassword(PasswordEntry entry) throws PasswordManagerException {
        if (entry == null) {
            throw new IllegalArgumentException("Password entry cannot be null");
        }
        
        // Check for duplicate names
        if (containsPasswordWithName(entry.getName())) {
            throw new PasswordManagerException("A password with the name '" + entry.getName() + "' already exists");
        }
        
        try {
            appendPasswordToFile(entry);
            passwords.add(entry);
            LOGGER.info("Successfully saved password entry: " + entry.getName());
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to save password entry", e);
            throw new PasswordManagerException("Failed to save password: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retrieves all password entries currently managed.
     * 
     * @return a copy of the list of password entries
     */
    public List<PasswordEntry> getAllPasswords() {
        return new ArrayList<>(passwords);
    }
    
    /**
     * Searches for a password entry by name.
     * 
     * @param name the name to search for
     * @return the password entry if found, null otherwise
     */
    public PasswordEntry findPasswordByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        return passwords.stream()
                .filter(entry -> entry.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Returns the number of password entries currently managed.
     * 
     * @return the count of password entries
     */
    public int getPasswordCount() {
        return passwords.size();
    }
    
    /**
     * Checks if a password with the given name exists.
     * 
     * @param name the name to check
     * @return true if a password with this name exists, false otherwise
     */
    public boolean containsPasswordWithName(String name) {
        return findPasswordByName(name) != null;
    }
    
    /**
     * Exports all passwords to a specified file in a formatted manner.
     * 
     * @param exportPath the path where to export passwords
     * @throws PasswordManagerException if export fails
     */
    public void exportPasswords(Path exportPath) throws PasswordManagerException {
        if (exportPath == null) {
            throw new IllegalArgumentException("Export path cannot be null");
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(exportPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeFileHeader(writer, "Password Export");
            
            writer.write("Total Passwords: " + passwords.size());
            writer.newLine();
            writer.write(SEPARATOR);
            writer.newLine();
            writer.newLine();
            
            for (int i = 0; i < passwords.size(); i++) {
                PasswordEntry entry = passwords.get(i);
                writer.write(String.format("Entry #%d:", i + 1));
                writer.newLine();
                writer.write(entry.toFileFormat());
                writer.newLine();
                writer.write("Password Strength: " + getPasswordStrengthDescription(entry));
                writer.newLine();
                writer.newLine();
            }
            
            LOGGER.info("Successfully exported " + passwords.size() + " passwords to: " + exportPath);
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to export passwords", e);
            throw new PasswordManagerException("Failed to export passwords: " + e.getMessage(), e);
        }
    }
    
    /**
     * Returns the file path where passwords are stored.
     * 
     * @return the file path
     */
    public Path getFilePath() {
        return filePath;
    }
    
    /**
     * Clears all passwords from memory and optionally from the file.
     * 
     * @param deleteFile if true, also deletes the password file
     * @throws PasswordManagerException if file deletion fails
     */
    public synchronized void clearPasswords(boolean deleteFile) throws PasswordManagerException {
        passwords.clear();
        
        if (deleteFile && Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                LOGGER.info("Deleted password file: " + filePath);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to delete password file", e);
                throw new PasswordManagerException("Failed to delete password file: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Reloads passwords from the file, discarding any unsaved changes.
     * 
     * @throws PasswordManagerException if reloading fails
     */
    public synchronized void reloadFromFile() throws PasswordManagerException {
        passwords.clear();
        
        if (Files.exists(filePath)) {
            try {
                loadPasswordsFromFile();
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to reload passwords from file", e);
                throw new PasswordManagerException("Failed to reload passwords: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Appends a password entry to the file.
     * 
     * @param entry the password entry to append
     * @throws IOException if writing fails
     */
    private void appendPasswordToFile(PasswordEntry entry) throws IOException {
        boolean isNewFile = !Files.exists(filePath);
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            if (isNewFile) {
                writeFileHeader(writer, "Password Manager - Secure Password Storage");
            }
            
            writer.write(entry.toFileFormat());
            writer.newLine();
        }
    }
    
    /**
     * Loads passwords from the file into memory.
     * This method parses the file format and reconstructs PasswordEntry objects.
     * 
     * @throws IOException if reading fails
     */
    private void loadPasswordsFromFile() throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        
        for (String line : lines) {
            line = line.trim();
            
            // Skip empty lines, separators, and header lines
            if (line.isEmpty() || line.startsWith("=") || line.startsWith("Password Manager") 
                || line.startsWith("Generated on") || line.contains("----------------------------------------")) {
                continue;
            }
            
            // Parse password entry line format: "Name: [name] | Password: [password] | Created: [timestamp] | Special Chars: [yes/no]"
            if (line.startsWith("Name: ")) {
                try {
                    PasswordEntry entry = parsePasswordEntryLine(line);
                    if (entry != null) {
                        passwords.add(entry);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to parse password entry line: " + line, e);
                }
            }
        }
        
        LOGGER.info("Loaded " + passwords.size() + " password entries from file");
    }
    
    /**
     * Parses a single line from the password file to create a PasswordEntry.
     * 
     * @param line the line to parse
     * @return the parsed PasswordEntry, or null if parsing fails
     */
    private PasswordEntry parsePasswordEntryLine(String line) {
        try {
            // Split by " | " to get the different parts
            String[] parts = line.split(" \\| ");
            
            if (parts.length >= 4) {
                String name = parts[0].substring(6).trim(); // Remove "Name: "
                String password = parts[1].substring(10).trim(); // Remove "Password: "
                boolean hasSpecialChars = parts[3].contains("Yes");
                
                return new PasswordEntry(name, password, hasSpecialChars);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing password entry line", e);
        }
        
        return null;
    }
    
    /**
     * Writes a formatted header to the file.
     * 
     * @param writer the BufferedWriter to write to
     * @param title the title for the header
     * @throws IOException if writing fails
     */
    private void writeFileHeader(BufferedWriter writer, String title) throws IOException {
        writer.write("================================================================================");
        writer.newLine();
        writer.write(title);
        writer.newLine();
        writer.write("Generated on: " + LocalDateTime.now().format(HEADER_FORMATTER));
        writer.newLine();
        writer.write("================================================================================");
        writer.newLine();
        writer.newLine();
    }
    
    /**
     * Gets a human-readable description of password strength.
     * 
     * @param entry the password entry to evaluate
     * @return strength description
     */
    private String getPasswordStrengthDescription(PasswordEntry entry) {
        PasswordGenerator generator = new PasswordGenerator();
        int strength = generator.validatePasswordStrength(entry.getPassword());
        
        if (strength >= 80) return "Very Strong (" + strength + "/100)";
        if (strength >= 60) return "Strong (" + strength + "/100)";
        if (strength >= 40) return "Medium (" + strength + "/100)";
        if (strength >= 20) return "Weak (" + strength + "/100)";
        return "Very Weak (" + strength + "/100)";
    }
    
    /**
     * Custom exception class for PasswordManager operations.
     */
    public static class PasswordManagerException extends Exception {
        /**
         * Constructs a new PasswordManagerException with the specified message.
         * 
         * @param message the exception message
         */
        public PasswordManagerException(String message) {
            super(message);
        }
        
        /**
         * Constructs a new PasswordManagerException with the specified message and cause.
         * 
         * @param message the exception message
         * @param cause the underlying cause
         */
        public PasswordManagerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}