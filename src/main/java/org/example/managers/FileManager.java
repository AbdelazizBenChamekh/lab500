package org.example.managers;

import org.example.models.*;
import org.example.utility.ConsoleReader;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Handles loading and saving the StudyGroup collection to a CSV file.
 * Uses basic file I/O and simple CSV parsing.
 */
public class FileManager {
    private final String filePath;
    private final ConsoleReader console; // Use ConsoleReader for output

    /**
     * Constructor.
     * @param filePath Path to the CSV data file (can be null).
     * @param console For printing messages/errors.
     */
    public FileManager(String filePath, ConsoleReader console) {
        this.filePath = filePath;
        this.console = console;
    }

    /**
     * Saves the collection to the CSV file. Overwrites existing file.
     * Prints error if filePath is null or save fails.
     */
    public void saveCollection(LinkedHashSet<StudyGroup> collection) {
        if (filePath == null) {
            console.printError("Cannot save: No file path specified (check environment variable).");
            return;
        }

        File file = new File(filePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            console.printError("Cannot save: Parent directory does not exist: " + file.getParentFile());
            return;
        }


        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (StudyGroup group : collection) {
                writer.println(group.toCsv()); // Rely on StudyGroup's CSV format
            }
            console.println("Collection saved successfully to " + filePath);
        } catch (IOException e) {
            console.printError("Could not write to file '" + filePath + "': " + e.getMessage());
        } catch (SecurityException e) {
            console.printError("Permission denied writing to file '" + filePath + "'.");
        }
    }

    /**
     * Loads the collection from the CSV file.
     * Handles basic errors like file not found or unreadable.
     * Skips lines with incorrect format or invalid data.
     * Initializes the IdGenerator.
     * @return The loaded collection (LinkedHashSet), or an empty set if loading fails.
     */
    public LinkedHashSet<StudyGroup> loadCollection() {
        LinkedHashSet<StudyGroup> collection = new LinkedHashSet<>();
        HashSet<Integer> loadedIds = new HashSet<>(); // Track IDs found in file

        if (filePath == null) {
            console.printError("Cannot load: No file path specified. Starting with empty collection.");
            IdGenerator.initialize(Collections.emptySet()); // Init generator for empty start
            return collection;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            console.println("Data file not found: '" + filePath + "'. Starting with empty collection.");
            IdGenerator.initialize(Collections.emptySet());
            return collection;
        }
        if (!file.canRead()) {
            console.printError("Cannot read data file: '" + filePath + "'. Check permissions. Starting empty.");
            IdGenerator.initialize(Collections.emptySet());
            return collection;
        }

        console.println("Loading collection from " + filePath + "...");
        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) continue;

                //CSV Parsing , split by comma (-)
                String[] data = trimmedLine.split(",");
                final int EXPECTED_FIELDS = 17; // Based on StudyGroup.toCsv()

                if (data.length != EXPECTED_FIELDS) {
                    console.printError("Skipping line " + lineNumber + ": Incorrect number of fields (expected " + EXPECTED_FIELDS + ", found " + data.length + ")");
                    continue;
                }

                try {
                    // Parse fields one by one with basic validation
                    int id = Integer.parseInt(data[0]);
                    if (id <= 0) throw new IllegalArgumentException("ID must be positive");
                    if (!loadedIds.add(id)) throw new IllegalArgumentException("Duplicate ID found: " + id);

                    String name = data[1];
                    Coordinates coords = new Coordinates(Integer.parseInt(data[2]), Integer.parseInt(data[3]));
                    LocalDate creationDate = LocalDate.parse(data[4]);
                    long studentsCount = Long.parseLong(data[5]);
                    Long shouldBeExpelled = data[6].isEmpty() ? null : Long.parseLong(data[6]);
                    FormOfEducation form = FormOfEducation.valueOf(data[7].toUpperCase());
                    Semester semester = data[8].isEmpty() ? null : Semester.valueOf(data[8].toUpperCase());

                    // Person data
                    String adminName = data[9];
                    Integer adminWeight = Integer.parseInt(data[10]);
                    Color eyeColor = data[11].isEmpty() ? null : Color.valueOf(data[11].toUpperCase());
                    Color hairColor = Color.valueOf(data[12].toUpperCase());
                    Country nationality = data[13].isEmpty() ? null : Country.valueOf(data[13].toUpperCase());
                    Location location = new Location(Integer.parseInt(data[14]), Double.parseDouble(data[15]), data[16].isEmpty() ? null : data[16]);

                    Person admin = new Person(adminName, adminWeight, eyeColor, hairColor, nationality, location);

                    // Create StudyGroup using the loading constructor
                    StudyGroup group = new StudyGroup(id, name, coords, creationDate, studentsCount, shouldBeExpelled, form, semester, admin);
                    collection.add(group);

                } catch (DateTimeParseException e) {
                    console.printError("Skipping line " + lineNumber + ": Invalid date format (expected YYYY-MM-DD). Details: " + e.getMessage());
                } catch (NumberFormatException e) {
                    console.printError("Skipping line " + lineNumber + ": Invalid number format in one of the fields. Details: " + e.getMessage());
                } catch (IllegalArgumentException e) {

                    console.printError("Skipping line " + lineNumber + ": Invalid data. Details: " + e.getMessage());
                } catch (Exception e) {
                    // Catch unexpected errors during line processing
                    console.printError("Skipping line " + lineNumber + ": Unexpected error. Details: " + e.getMessage());
                }
            }
            console.println("Loaded " + collection.size() + " elements.");

        } catch (IOException e) {
            console.printError("Error reading file '" + filePath + "': " + e.getMessage());
            collection.clear(); // Start empty on read error
            loadedIds.clear();
        } finally {
            // Initialize ID generator based on IDs successfully loaded
            IdGenerator.initialize(loadedIds);
        }

        return collection;
    }
}
