package org.example.server.core;

import org.example.common.models.StudyGroup;
import org.example.server.exceptions.ExitObliged;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileManager using Java built-in serialization to save/load collection.
 */
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
        else {
            System.err.println("FM_ERROR: " + message + (e != null ? " - " + e.getMessage() : ""));
            if (e != null) e.printStackTrace(System.err);
        }
    }

    private void logInfo(String message) {
        if (logger != null) logger.info(message);
        else System.out.println("FM_INFO: " + message);
    }

    /**
     * Saves the collection to a file using Java serialization.
     */
    public void saveCollection(LinkedHashSet<StudyGroup> collection) {
        if (filePath == null) {
            logError("Cannot save: No file path specified.", null);
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(collection);
            logInfo("Collection saved successfully to " + filePath);
        } catch (IOException e) {
            logError("Could not write to file '" + filePath + "'", e);
        }
    }

    /**
     * Checks if the file exists and is accessible.
     * Throws ExitObliged if file is not accessible.
     */
    public void findFile() throws ExitObliged {
        if (filePath == null || filePath.isEmpty()) {
            logError("File path is not specified.", null);
            throw new ExitObliged();
        }

        File file = new File(filePath);
        if (!file.exists()) {
            logInfo("Data file does not exist at " + filePath + ". It will be created on save.");
        } else if (!file.isFile()) {
            logError("Path exists but is not a file: " + filePath, null);
            throw new ExitObliged();
        } else if (!file.canRead()) {
            logError("File exists but cannot be read: " + filePath, null);
            throw new ExitObliged();
        } else {
            logInfo("Data file found and readable: " + filePath);
        }
    }

    /**
     * Loads the collection from file or initializes empty collection.
     * Throws ExitObliged if loading fails critically.
     */
    public void createObjects() throws ExitObliged {
        LinkedHashSet<StudyGroup> loadedCollection = loadCollection();
        if (loadedCollection.isEmpty()) {
            logInfo("Starting with an empty collection.");
        } else {
            logInfo("Loaded collection with " + loadedCollection.size() + " elements.");
        }
        // You may want to store this collection in a field or pass it back as needed
    }

    /**
     * Loads the collection from a file using Java serialization.
     * Returns empty collection if file not found or error occurs.
     */
    @SuppressWarnings("unchecked")
    public LinkedHashSet<StudyGroup> loadCollection() {
        if (filePath == null) {
            logError("Cannot load: No file path specified.", null);
            return new LinkedHashSet<>();
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            logInfo("Data file not found or unreadable: '" + filePath + "'. Starting with empty collection.");
            return new LinkedHashSet<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof LinkedHashSet) {
                logInfo("Collection loaded successfully from " + filePath);
                return (LinkedHashSet<StudyGroup>) obj;
            } else {
                logError("File content is not a valid collection. Starting empty.", null);
                return new LinkedHashSet<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            logError("Error reading file '" + filePath + "'", e);
            return new LinkedHashSet<>();
        }
    }
}
