package org.example.server.core;

import org.example.common.models.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManager {
    private final String filePath;
    private final Logger logger;

    public FileManager(String filePath, Logger logger) {
        this.filePath = filePath;
        this.logger = logger;
        if (this.logger == null) {
            System.err.println("FileManager Warning: Logger not provided, using System.err/out.");
        }
    }

    private void logError(String message, Exception e) {
        if (logger != null) logger.log(Level.SEVERE, message, e);
        else { System.err.println("FM_ERROR: " + message + (e != null ? " - " + e.getMessage() : "")); if (e != null) e.printStackTrace(System.err); }
    }
    private void logError(String message) { logError(message, null); }
    private void logInfo(String message) { if (logger != null) logger.info(message); else System.out.println("FM_INFO: " + message); }
    private void logWarning(String message) { if (logger != null) logger.warning(message); else System.err.println("FM_WARN: " + message); }

    public void saveCollection(LinkedHashSet<StudyGroup> collection) {
        if (filePath == null) {
            logError("Cannot save: No file path specified.");
            return;
        }
        File file = new File(filePath);
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            for (StudyGroup group : collection) {
                writer.println(group.toCsv());
            }
            logInfo("Collection saved successfully to " + filePath);
        } catch (IOException e) {
            logError("Could not write to file '" + filePath + "'", e);
        } catch (SecurityException e) {
            logError("Permission denied writing to file '" + filePath + "'", e);
        }
    }

    public LinkedHashSet<StudyGroup> loadCollection() {
        LinkedHashSet<StudyGroup> collection = new LinkedHashSet<>();
        HashSet<Integer> loadedIds = new HashSet<>();

        if (filePath == null) {
            logWarning("Cannot load: No file path specified. Starting empty collection.");
            IdGenerator.initialize(Collections.emptySet());
            return collection;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            logInfo("Data file not found: '" + filePath + "'. Starting empty collection.");
            IdGenerator.initialize(Collections.emptySet());
            return collection;
        }
        if (!file.isFile()){
            logError("Path is a directory, not a file: '" + filePath + "'. Starting empty.");
            IdGenerator.initialize(Collections.emptySet());
            return collection;
        }
        if (!file.canRead()) {
            logError("Cannot read data file: '" + filePath + "'. Starting empty.");
            IdGenerator.initialize(Collections.emptySet());
            return collection;
        }

        logInfo("Loading collection from " + filePath + "...");
        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmedLine = line.trim();
                if (trimmedLine.isEmpty()) continue;

                String[] data = trimmedLine.split(",");
                final int EXPECTED_FIELDS = 17;

                if (data.length != EXPECTED_FIELDS) {
                    logWarning("Skipping line " + lineNumber + ": Incorrect number of fields (expected " + EXPECTED_FIELDS + ", found " + data.length + ")");
                    continue;
                }
                try {
                    int id = Integer.parseInt(data[0].trim());
                    if (id <= 0) throw new IllegalArgumentException("ID must be positive");
                    if (!loadedIds.add(id)) throw new IllegalArgumentException("Duplicate ID found: " + id);
                    String name = data[1];
                    int coordX = Integer.parseInt(data[2].trim());
                    int coordY = Integer.parseInt(data[3].trim());
                    Coordinates coords = new Coordinates(coordX, coordY);
                    LocalDate creationDate = LocalDate.parse(data[4].trim());
                    long studentsCount = Long.parseLong(data[5].trim());
                    String expelledStr = data[6].trim();
                    Long shouldBeExpelled = expelledStr.isEmpty() ? null : Long.parseLong(expelledStr);
                    FormOfEducation form = FormOfEducation.valueOf(data[7].trim().toUpperCase());
                    String semesterStr = data[8].trim();
                    Semester semester = semesterStr.isEmpty() ? null : Semester.valueOf(semesterStr.toUpperCase());
                    String adminName = data[9];
                    int adminWeight = Integer.parseInt(data[10].trim());
                    String eyeColorStr = data[11].trim();
                    Color eyeColor = eyeColorStr.isEmpty() ? null : Color.valueOf(eyeColorStr.toUpperCase());
                    Color hairColor = Color.valueOf(data[12].trim().toUpperCase());
                    String nationStr = data[13].trim();
                    Country nationality = nationStr.isEmpty() ? null : Country.valueOf(nationStr.toUpperCase());
                    int locX = Integer.parseInt(data[14].trim());
                    double locY = Double.parseDouble(data[15].trim());
                    String locNameStr = data[16];
                    String locName = locNameStr.isEmpty() ? null : locNameStr;
                    Location location = new Location(locX, locY, locName);
                    Person admin = new Person(adminName, adminWeight, eyeColor, hairColor, nationality, location);
                    StudyGroup group = new StudyGroup(id, name, coords, creationDate, studentsCount, shouldBeExpelled, form, semester, admin);
                    collection.add(group);
                } catch (DateTimeParseException e) {
                    logWarning("Skipping line " + lineNumber + ": Invalid date format. " + e.getMessage());
                } catch (NumberFormatException e) {
                    logWarning("Skipping line " + lineNumber + ": Invalid number format. " + e.getMessage());
                } catch (IllegalArgumentException e) {
                    logWarning("Skipping line " + lineNumber + ": Invalid data. " + e.getMessage());
                } catch (Exception e) {
                    logError("Skipping line " + lineNumber + ": Unexpected error.", e);
                }
            }
            logInfo("Loaded " + collection.size() + " elements.");
        } catch (IOException e) {
            logError("Error reading file '" + filePath + "'", e);
            collection.clear();
            loadedIds.clear();
        } finally {
            IdGenerator.initialize(loadedIds);
        }
        return collection;
    }
}