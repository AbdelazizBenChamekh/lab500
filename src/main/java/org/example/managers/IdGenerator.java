package org.example.managers;

import java.util.Collections;
import java.util.Set;

/**
 * Simple generator for unique positive IDs.
 * Starts after the maximum ID found during initialization.
 */
public class IdGenerator {
    private static int currentMaxId = 0;

    /**
     * Finds the maximum ID in the loaded set and prepares the generator.
     * Should be called once after loading the collection.
     */
    public static void initialize(Set<Integer> existingIds) {
        if (existingIds == null || existingIds.isEmpty()) {
            currentMaxId = 0;
        } else {
            currentMaxId = Collections.max(existingIds);
        }
        System.out.println("IdGenerator initialized. Next ID: " + (currentMaxId + 1));
    }

    /** Generates the next available unique ID. */
    public static int generateId() {
        currentMaxId++;
        return currentMaxId;
    }

}