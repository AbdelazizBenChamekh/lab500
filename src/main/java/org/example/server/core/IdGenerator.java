// File: src/main/java/org/example/server/core/IdGenerator.java
package org.example.server.core; // Adjust package if needed

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger; // Using AtomicInteger for potential future thread safety

/**
 * Generates unique, sequential, positive IDs for StudyGroup objects on the server.
 * Starts after the maximum ID found during initialization. Thread-safe.
 */
public class IdGenerator {
    // Use AtomicInteger for safe incrementing, even if server is currently single-threaded
    private static final AtomicInteger currentMaxId = new AtomicInteger(0);

    /**
     * Initializes the generator based on existing IDs loaded from the file.
     * Finds the maximum existing ID and sets the generator to start from the next value.
     * Should be called once by FileManager after loading the collection.
     * @param existingIds A set of IDs already present in the collection.
     */
    public static synchronized void initialize(Set<Integer> existingIds) {
        int maxId = 0;
        if (existingIds != null && !existingIds.isEmpty()) {
            // Find the maximum ID present in the set
            for (Integer id : existingIds) {
                if (id != null && id > maxId) { // Handle potential nulls just in case
                    maxId = id;
                }
            }
            // Alternative using streams:
            // maxId = existingIds.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).max().orElse(0);
        }
        currentMaxId.set(maxId); // Set the AtomicInteger to the found max
        System.out.println("[IdGenerator] Initialized. Current max ID: " + maxId + ". Next ID: " + (maxId + 1));
    }

    /**
     * Generates the next available unique ID by atomically incrementing the counter.
     * @return A new unique ID greater than 0.
     */
    public static int generateId() {
        return currentMaxId.incrementAndGet(); // Atomically increments and returns the new value
    }

    /** Resets the generator (useful for testing, use with caution). */
    public static synchronized void reset() {
        currentMaxId.set(0);
        System.out.println("[IdGenerator] Reset.");
    }
}