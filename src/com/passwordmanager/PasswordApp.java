package com.passwordmanager;

import com.passwordmanager.PasswordManager.PasswordManagerException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application class for the Password Manager system.
 * This class provides a command-line interface for password generation,
 * storage, and management operations. It coordinates the interaction between
 * PasswordGenerator, PasswordManager, and PasswordEntry classes.
 * 
 * @author Jose Mondragon
 * @version 1.0
 * @since 2025-06-03
 */
public class PasswordApp {
    
    /** Logger for this class */
    private static final Logger LOGGER = Logger.getLogger(PasswordApp.class.getName());
    
    /** Password generator instance */
    private final PasswordGenerator passwordGenerator;
    
    /** Password manager instance for file operations */
    private final PasswordManager passwordManager;
    
    /** Scanner for user input */
    private final Scanner scanner;
    
    /** Application running state */
    private boolean running;
    
    /**
     * Constructs a new PasswordApp with default settings.
     * Initializes the password generator and manager with default configurations.
     * 
     * @throws PasswordManagerException if initialization fails
     */
    public PasswordApp() throws PasswordManagerException {
        this.passwordGenerator = new PasswordGenerator(8, 20); // 8-20 character range
        this.passwordManager = new PasswordManager();
        this.scanner = new Scanner(System.in);
        this.running = false;
        
        LOGGER.info("Password Manager Application initialized successfully");
    }
    
    /**
     * Starts the password manager application and displays the main menu.
     * This method runs the main application loop until the user chooses to exit.
     */
    public void start() {
        running = true;
        displayWelcomeMessage();
        
        while (running) {
            try {
                displayMainMenu();
                int choice = getUserChoice();
                processMenuChoice(choice);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error in main application loop", e);
                System.err.println("An error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
        
        cleanup();
    }
    
    /**
     * Displays the welcome message and application information.
     */
    private void displayWelcomeMessage() {
        System.out.println("================================================================================");
        System.out.println("                        SECURE PASSWORD MANAGER v1.0");
        System.out.println("                            Created by Jose Mondragon");
        System.out.println("================================================================================");
        System.out.println("Welcome to the Secure Password Manager!");
        System.out.println("This application helps you generate and store strong, secure passwords.");
        System.out.println("Storage location: " + passwordManager.getFilePath().toAbsolutePath());
        System.out.println("Current passwords stored: " + passwordManager.getPasswordCount());
        System.out.println();
    }
    
    /**
     * Displays the main menu options to the user.
     */
    private void displayMainMenu() {
        System.out.println("================================================================================");
        System.out.println("                                 MAIN MENU");
        System.out.println("================================================================================");
        System.out.println("1. Generate and Save Password (without special characters)");
        System.out.println("2. Generate and Save Password (with special characters)");
        System.out.println("3. View All Stored Passwords");
        System.out.println("4. Search for Password by Name");
        System.out.println("5. Export Passwords to File");
        System.out.println("6. Password Strength Analyzer");
        System.out.println("7. Application Statistics");
        System.out.println("8. Settings");
        System.out.println("9. Exit Application");
        System.out.println("================================================================================");
        System.out.print("Please select an option (1-9): ");
    }
    
    /**
     * Gets and validates user input for menu choice.
     * 
     * @return the validated menu choice
     */
    private int getUserChoice() {
        try {
            String input = scanner.nextLine().trim();
            int choice = Integer.parseInt(input);
            
            if (choice < 1 || choice > 9) {
                System.out.println("Invalid choice. Please select a number between 1 and 9.");
                return getUserChoice();
            }
            
            return choice;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number between 1 and 9.");
            return getUserChoice();
        }
    }
    
    /**
     * Processes the user's menu choice and calls the appropriate method.
     * 
     * @param choice the user's menu choice
     */
    private void processMenuChoice(int choice) {
        System.out.println();
        
        switch (choice) {
            case 1:
                generateAndSavePassword(false);
                break;
            case 2:
                generateAndSavePassword(true);
                break;
            case 3:
                viewAllPasswords();
                break;
            case 4:
                searchPasswordByName();
                break;
            case 5:
                exportPasswords();
                break;
            case 6:
                analyzePasswordStrength();
                break;
            case 7:
                showApplicationStatistics();
                break;
            case 8:
                showSettings();
                break;
            case 9:
                exitApplication();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        
        if (running && choice != 9) {
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    /**
     * Generates a new password and saves it with a user-provided name.
     * 
     * @param includeSpecialChars whether to include special characters in the password
     */
    private void generateAndSavePassword(boolean includeSpecialChars) {
        System.out.println("--- Generate and Save Password ---");
        System.out.println("Special characters: " + (includeSpecialChars ? "Enabled" : "Disabled"));
        System.out.println();
        
        System.out.print("Enter a name for this password: ");
        String name = scanner.nextLine().trim();
        
        if (name.isEmpty()) {
            System.out.println("Error: Password name cannot be empty.");
            return;
        }
        
        if (passwordManager.containsPasswordWithName(name)) {
            System.out.println("Error: A password with this name already exists.");
            return;
        }
        
        try {
            String password = passwordGenerator.generatePassword(includeSpecialChars);
            PasswordEntry entry = new PasswordEntry(name, password, includeSpecialChars);
            
            // Display generated password
            System.out.println("\nGenerated Password Details:");
            System.out.println("Name: " + entry.getName());
            System.out.println("Password: " + entry.getPassword());
            System.out.println("Length: " + entry.getPasswordLength() + " characters");
            System.out.println("Special Characters: " + (entry.hasSpecialCharacters() ? "Yes" : "No"));
            System.out.println("Strength: " + getPasswordStrengthDescription(password));
            System.out.println("Created: " + entry.getFormattedTimestamp());
            
            // Confirm save
            System.out.print("\nSave this password? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                passwordManager.savePassword(entry);
                System.out.println("✓ Password saved successfully!");
            } else {
                System.out.println("Password not saved.");
            }
            
        } catch (PasswordManagerException e) {
            System.err.println("Error saving password: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Failed to save password", e);
        }
    }
    
    /**
     * Displays all stored passwords in a formatted table.
     */
    private void viewAllPasswords() {
        System.out.println("--- All Stored Passwords ---");
        
        List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        
        if (passwords.isEmpty()) {
            System.out.println("No passwords stored yet.");
            return;
        }
        
        System.out.println("Total passwords: " + passwords.size());
        System.out.println();
        
        // Display table header
        System.out.printf("%-4s %-20s %-25s %-8s %-15s %-20s%n", 
                "#", "Name", "Password", "Length", "Special Chars", "Created");
        System.out.println("--------------------------------------------------------------------------------");
        
        // Display each password
        for (int i = 0; i < passwords.size(); i++) {
            PasswordEntry entry = passwords.get(i);
            System.out.printf("%-4d %-20s %-25s %-8d %-15s %-20s%n",
                    i + 1,
                    truncateString(entry.getName(), 20),
                    entry.getPassword(),
                    entry.getPasswordLength(),
                    entry.hasSpecialCharacters() ? "Yes" : "No",
                    entry.getFormattedTimestamp());
        }
    }
    
    /**
     * Searches for a password by name and displays the results.
     */
    private void searchPasswordByName() {
        System.out.println("--- Search Password by Name ---");
        System.out.print("Enter the password name to search for: ");
        String searchName = scanner.nextLine().trim();
        
        if (searchName.isEmpty()) {
            System.out.println("Search name cannot be empty.");
            return;
        }
        
        PasswordEntry entry = passwordManager.findPasswordByName(searchName);
        
        if (entry != null) {
            System.out.println("\n✓ Password found:");
            System.out.println("Name: " + entry.getName());
            System.out.println("Password: " + entry.getPassword());
            System.out.println("Length: " + entry.getPasswordLength() + " characters");
            System.out.println("Special Characters: " + (entry.hasSpecialCharacters() ? "Yes" : "No"));
            System.out.println("Strength: " + getPasswordStrengthDescription(entry.getPassword()));
            System.out.println("Created: " + entry.getFormattedTimestamp());
        } else {
            System.out.println("✗ No password found with the name: " + searchName);
        }
    }
    
    /**
     * Exports all passwords to a user-specified file.
     */
    private void exportPasswords() {
        System.out.println("--- Export Passwords ---");
        
        if (passwordManager.getPasswordCount() == 0) {
            System.out.println("No passwords to export.");
            return;
        }
        
        System.out.print("Enter filename for export (e.g., 'my_passwords.txt'): ");
        String filename = scanner.nextLine().trim();
        
        if (filename.isEmpty()) {
            filename = "password_export_" + System.currentTimeMillis() + ".txt";
            System.out.println("Using default filename: " + filename);
        }
        
        try {
            passwordManager.exportPasswords(Paths.get(filename));
            System.out.println("✓ Passwords exported successfully to: " + filename);
        } catch (PasswordManagerException e) {
            System.err.println("Error exporting passwords: " + e.getMessage());
            LOGGER.log(Level.WARNING, "Failed to export passwords", e);
        }
    }
    
    /**
     * Analyzes the strength of a user-provided password.
     */
    private void analyzePasswordStrength() {
        System.out.println("--- Password Strength Analyzer ---");
        System.out.print("Enter a password to analyze: ");
        String password = scanner.nextLine();
        
        if (password.isEmpty()) {
            System.out.println("Password cannot be empty.");
            return;
        }
        
        int strength = passwordGenerator.validatePasswordStrength(password);
        String description = getPasswordStrengthDescription(password);
        
        System.out.println("\nPassword Analysis Results:");
        System.out.println("Password: " + password);
        System.out.println("Length: " + password.length() + " characters");
        System.out.println("Strength Score: " + strength + "/100");
        System.out.println("Strength Level: " + description);
        
        // Provide recommendations
        System.out.println("\nRecommendations:");
        if (strength < 60) {
            System.out.println("- Consider using a longer password (12+ characters)");
            System.out.println("- Include uppercase and lowercase letters");
            System.out.println("- Add numbers and special characters");
            System.out.println("- Avoid common words or patterns");
        } else if (strength < 80) {
            System.out.println("- Your password is good, but could be stronger");
            System.out.println("- Consider adding more special characters");
            System.out.println("- Ensure it's not based on personal information");
        } else {
            System.out.println("- Excellent password strength!");
            System.out.println("- Your password meets security best practices");
        }
    }
    
    /**
     * Displays application statistics and information.
     */
    private void showApplicationStatistics() {
        System.out.println("--- Application Statistics ---");
        
        List<PasswordEntry> passwords = passwordManager.getAllPasswords();
        
        System.out.println("Total passwords stored: " + passwords.size());
        System.out.println("Storage file: " + passwordManager.getFilePath().toAbsolutePath());
        
        if (!passwords.isEmpty()) {
            long withSpecialChars = passwords.stream()
                    .mapToLong(entry -> entry.hasSpecialCharacters() ? 1 : 0)
                    .sum();
            
            double avgLength = passwords.stream()
                    .mapToInt(PasswordEntry::getPasswordLength)
                    .average()
                    .orElse(0.0);
            
            System.out.println("Passwords with special characters: " + withSpecialChars);
            System.out.println("Passwords without special characters: " + (passwords.size() - withSpecialChars));
            System.out.printf("Average password length: %.1f characters%n", avgLength);
            
            // Show strength distribution
            long veryStrong = 0, strong = 0, medium = 0, weak = 0;
            for (PasswordEntry entry : passwords) {
                int strength = passwordGenerator.validatePasswordStrength(entry.getPassword());
                if (strength >= 80) veryStrong++;
                else if (strength >= 60) strong++;
                else if (strength >= 40) medium++;
                else weak++;
            }
            
            System.out.println("\nPassword Strength Distribution:");
            System.out.println("Very Strong (80-100): " + veryStrong);
            System.out.println("Strong (60-79): " + strong);
            System.out.println("Medium (40-59): " + medium);
            System.out.println("Weak (0-39): " + weak);
        }
        
        System.out.println("\nGenerator Settings:");
        System.out.println("Min Length: " + passwordGenerator.getMinLength());
        System.out.println("Max Length: " + passwordGenerator.getMaxLength());
    }
    
    /**
     * Displays and allows modification of application settings.
     */
    private void showSettings() {
        System.out.println("--- Application Settings ---");
        System.out.println("Current Settings:");
        System.out.println("1. Minimum password length: " + passwordGenerator.getMinLength());
        System.out.println("2. Maximum password length: " + passwordGenerator.getMaxLength());
        System.out.println("3. Storage location: " + passwordManager.getFilePath().toAbsolutePath());
        System.out.println();
        System.out.println("Settings modification options:");
        System.out.println("1. Change minimum password length");
        System.out.println("2. Change maximum password length");
        System.out.println("3. Return to main menu");
        System.out.print("Select option (1-3): ");
        
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            
            switch (choice) {
                case 1:
                    changeMinLength();
                    break;
                case 2:
                    changeMaxLength();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }
    
    /**
     * Changes the minimum password length setting.
     */
    private void changeMinLength() {
        System.out.print("Enter new minimum length (4-128): ");
        try {
            int newMin = Integer.parseInt(scanner.nextLine().trim());
            passwordGenerator.setMinLength(newMin);
            System.out.println("✓ Minimum length updated to: " + newMin);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Changes the maximum password length setting.
     */
    private void changeMaxLength() {
        System.out.print("Enter new maximum length (must be >= min length): ");
        try {
            int newMax = Integer.parseInt(scanner.nextLine().trim());
            passwordGenerator.setMaxLength(newMax);
            System.out.println("✓ Maximum length updated to: " + newMax);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Exits the application gracefully.
     */
    private void exitApplication() {
        System.out.println("--- Exit Application ---");
        System.out.println("Thank you for using the Secure Password Manager!");
        System.out.println("Your passwords have been safely stored.");
        System.out.println("Created by Jose Mondragon - Software Engineering Best Practices");
        running = false;
    }
    
    /**
     * Performs cleanup operations before application shutdown.
     */
    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
        LOGGER.info("Password Manager Application shutdown completed");
    }
    
    /**
     * Gets a human-readable password strength description.
     * 
     * @param password the password to evaluate
     * @return strength description
     */
    private String getPasswordStrengthDescription(String password) {
        int strength = passwordGenerator.validatePasswordStrength(password);
        
        if (strength >= 80) return "Very Strong";
        if (strength >= 60) return "Strong";
        if (strength >= 40) return "Medium";
        if (strength >= 20) return "Weak";
        return "Very Weak";
    }
    
    /**
     * Truncates a string to the specified length with ellipsis if needed.
     * 
     * @param str the string to truncate
     * @param length the maximum length
     * @return the truncated string
     */
    private String truncateString(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length - 3) + "...";
    }
    
    /**
     * Main method - entry point of the application.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            PasswordApp app = new PasswordApp();
            app.start();
        } catch (PasswordManagerException e) {
            System.err.println("Failed to start Password Manager: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Application startup failed", e);
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Unexpected error occurred: " + e.getMessage());
            LOGGER.log(Level.SEVERE, "Unexpected application error", e);
            System.exit(1);
        }
    }
}