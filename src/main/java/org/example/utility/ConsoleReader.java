package org.example.utility;

import org.example.models.*;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for reading user input from the console with basic validation.
 * Supports interactive mode (with prompts) and script mode (no prompts).
 */
public class ConsoleReader {
    private final Scanner scanner;
    private boolean scriptMode = false; // Flag to control prompt printing

    /**
     * Constructor.
     * @param scanner The scanner to use (e.g., System.in or a file scanner).
     */
    public ConsoleReader(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Enables or disables script mode. In script mode, prompts are suppressed.
     * @param scriptMode true to run without prompts, false for interactive mode.
     */
    public void setScriptMode(boolean scriptMode) {
        this.scriptMode = scriptMode;
    }

    // Basic Prints
    public void print(String message) {
        System.out.print(message); }
    public void println(String message) {
        System.out.println(message); }
    public void printError(String message) {
        System.err.println("ERROR: " + message); }

    // Only prints the prompt if NOT in script mode
    private void printPrompt(String prompt) {
        if (!scriptMode) {
            print(prompt);
        }
    }


    /** Reads a non-empty String. */
    public String readNotEmptyString(String prompt) {
        String input;
        while (true) {
            printPrompt(prompt); // Use helper
            try {
                input = scanner.nextLine().trim();
                if (!input.isEmpty()) {
                    return input;
                } else {
                    printError("Input cannot be empty."); // Error always printed
                }
            } catch (NoSuchElementException e) {
                printError("Input stream closed. Cannot read.");
                throw e;
            }
        }
    }

    /** Reads a String that can be null (if user/script enters empty string). */
    public String readNullableString(String prompt) {
        printPrompt(prompt + (scriptMode ? "" : " (press Enter for null): ")); // Adjust prompt slightly
        try {
            String input = scanner.nextLine().trim();
            // if (scriptMode) { System.out.println("  <-- " + input); } // Optional echo
            return input.isEmpty() ? null : input;
        } catch (NoSuchElementException e) {
            printError("Input stream closed. Cannot read.");
            throw e;
        }
    }

    /** Reads an Integer, ensuring it's non-null. */
    public int readInt(String prompt) {
        Integer value;
        while (true) {
            printPrompt(prompt);
            try {
                String line = scanner.nextLine().trim();
                value = Integer.parseInt(line);
                return value;
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please enter an integer.");
            } catch (NoSuchElementException e) {
                printError("Input stream closed. Cannot read.");
                throw e;
            }
        }
    }

    /** Reads an Integer greater than a threshold. */
    public int readIntGreaterThan(String prompt, int threshold) {
        int value;
        while (true) {
            value = readInt(prompt);
            if (value > threshold) {
                return value;
            } else {
                printError("Value must be greater than " + threshold + ".");
            }
        }
    }

    /** Reads an Integer, allowing null (if user/script enters empty string). */
    public Integer readNullableInt(String prompt) {
        while(true) {
            printPrompt(prompt + (scriptMode ? "" : " (press Enter for null): "));
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    return null;
                }
                try {
                    return Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    printError("Invalid number format. Please enter an integer or leave blank for null."); // Error always printed
                }
            } catch (NoSuchElementException e) {
                printError("Input stream closed. Cannot read.");
                throw e;
            }
        }
    }

    /** Reads a long value, ensuring it's greater than a threshold. */
    public long readLongGreaterThan(String prompt, long threshold) {
        long value;
        while (true) {
            printPrompt(prompt); // Use helper
            try {
                String line = scanner.nextLine().trim();
                value = Long.parseLong(line);
                if (value > threshold) {
                    return value;
                } else {
                    printError("Value must be greater than " + threshold + ".");
                }
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please enter a whole number.");
            } catch (NoSuchElementException e) {
                printError("Input stream closed. Cannot read.");
                throw e;
            }
        }
    }

    /** Reads a nullable Long value, ensuring it's > 0 if provided. */
    public Long readNullableLongGreaterThanZero(String prompt) {
        Long value;
        while (true) {
            printPrompt(prompt + (scriptMode ? " (> 0)" : " (> 0, press Enter for null): "));
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    return null;
                }
                value = Long.parseLong(input);
                if (value > 0) {
                    return value;
                } else {
                    printError("Value must be greater than 0 if provided.");
                }
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please enter a whole number or leave blank.");
            } catch (NoSuchElementException e) {
                printError("Input stream closed. Cannot read.");
                throw e;
            }
        }
    }

    /** Reads a non-null double. */
    public double readDouble(String prompt) {
        double value;
        while (true) {
            printPrompt(prompt);
            try {
                String line = scanner.nextLine().trim();
                value = Double.parseDouble(line);
                return value;
            } catch (NumberFormatException e) {
                printError("Invalid number format. Please enter a number (e.g., 12.34).");
            } catch (NoSuchElementException e) {
                printError("Input stream closed. Cannot read.");
                throw e;
            }
        }
    }

    /** Reads an integer within a specific range (inclusive). */
    public int readIntInRange(String prompt, int min, int max) {
        int value;
        while (true) {
            value = readInt(prompt);
            if (value >= min && value <= max) {
                return value;
            } else {
                printError("Value must be between " + min + " and " + max + ".");
            }
        }
    }

    /** Reads an Enum numerically (ordinal + 1). Suppresses prompts in script mode. */
    public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass, boolean allowNull) {
        T[] enumValues = enumClass.getEnumConstants();
        if (enumValues == null || enumValues.length == 0) {
            printError("Cannot read enum " + enumClass.getSimpleName() + ": no values exist.");
            return null;
        }

        if (!scriptMode) {
            println(prompt);
            for (int i = 0; i < enumValues.length; i++) {
                println("  " + (i + 1) + ". " + enumValues[i].name());
            }
        }
        // Prepare prompt suffix (always needed for readNullableInt)
        String suffix = allowNull ? " (1-" + enumValues.length + ", or press Enter for null): " : " (1-" + enumValues.length + "): ";

        Integer choice = null;
        while(true) {
            try {
                // readNullableInt will use scriptMode to decide whether to print "> Choice..."
                choice = readNullableInt("> Choice" + suffix);

                if (choice == null) {
                    if (allowNull) {
                        return null;
                    } else {
                        printError("This field cannot be null. Please choose a number.");
                        continue;
                    }
                }

                if (choice >= 1 && choice <= enumValues.length) {
                    return enumValues[choice - 1];
                } else {
                    printError("Invalid choice. Please enter a number between 1 and " + enumValues.length + ".");
                }

            } catch (NoSuchElementException e) {
                throw e;
            } catch (Exception e) {
                printError("An error occurred reading the choice: " + e.getMessage());
            }
        }
    }

    //Complex reading of objects that will be used later by FileManager

    public Coordinates readCoordinates() {
        // Only print header in interactive mode
        if (!scriptMode) println("--- Enter Coordinates ---");
        int x = readInt("Enter X coordinate (integer): ");
        int y = readIntInRange("Enter Y coordinate (integer, max 405): ", Integer.MIN_VALUE, 405);
        return new Coordinates(x, y);
    }

    public Location readLocation() {
        if (!scriptMode) println("--- Enter Location ---");
        int x = readInt("Enter Location X coordinate (integer): ");
        double y = readDouble("Enter Location Y coordinate (number): ");
        String name = readNullableString("Enter Location name");
        return new Location(x, y, name);
    }

    public Person readPerson() {
        if (!scriptMode) println("--- Enter Group Admin Details ---");
        String name = readNotEmptyString("Enter Admin name: ");
        int weight = readIntGreaterThan("Enter Admin weight (integer > 0): ", 0);
        Color eyeColor = readEnum("Choose Admin eye color", Color.class, true);
        Color hairColor = readEnum("Choose Admin hair color", Color.class, false);
        Country nationality = readEnum("Choose Admin nationality", Country.class, true);
        Location location = readLocation();
        return new Person(name, weight, eyeColor, hairColor, nationality, location);
    }
}