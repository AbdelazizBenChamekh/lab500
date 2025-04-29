package org.example.server.core;

import org.example.common.models.FormOfEducation;
import org.example.common.models.Person;
import org.example.common.models.StudyGroup;

import org.example.client.ConsoleReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the main collection of StudyGroup objects (LinkedHashSet) on the server.
 * Handles operations like add, update, remove, clear, info, etc.
 * Uses Stream API for collection processing **
 */
public class CollectionManager {
    private LinkedHashSet<StudyGroup> studyGroups;
    private final FileManager fileManager;
    private final LocalDateTime initializationTime;
    private final ConsoleReader console;

    /**
     * Constructor. Loads the collection using FileManager.
     */
    public CollectionManager(FileManager fileManager, ConsoleReader console) {
        this.fileManager = fileManager;
        this.console = console;
        this.initializationTime = LocalDateTime.now();
        this.studyGroups = fileManager.loadCollection();
        console.println("CollectionManager initialized. Loaded " + studyGroups.size() + " elements.");
    }

    /** Returns an unmodifiable view of the collection instance. */
    public Set<StudyGroup> getCollection() {
        return Collections.unmodifiableSet(studyGroups);
    }

    /** Gets basic info about the collection. */
    public String getInfo() {
        return "Collection Type: " + studyGroups.getClass().getSimpleName() + "\n" +
                "Initialization Date: " + initializationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                "Number of Elements: " + studyGroups.size();
    }

    /** Adds a new element with a generated ID. */
    public void addElement(StudyGroup groupData) {
        try {
            int newId = IdGenerator.generateId();
            StudyGroup newGroup = new StudyGroup(
                    newId, groupData.getName(), groupData.getCoordinates(),
                    groupData.getStudentsCount(), groupData.getShouldBeExpelled(),
                    groupData.getFormOfEducation(), groupData.getSemesterEnum(),
                    groupData.getGroupAdmin());
            if (studyGroups.add(newGroup)) {
                console.println("Element added with ID: " + newId);
            } else {
                console.printError("INTERNAL ERROR: Failed to add element with presumably unique ID " + newId);
            }
        } catch (IllegalArgumentException e) {
            console.printError("Failed to add element due to validation error: " + e.getMessage());
        }
    }

    /** Finds an element by ID using Stream API. Returns null if not found. */
    public StudyGroup findById(int id) {
        return studyGroups.stream()
                .filter(group -> group.getId() == id)
                .findFirst() // Find the first match
                .orElse(null);
    }

    /** Updates an existing element, preserving ID and creation date. */
    public boolean updateElement(int id, StudyGroup updatedData) {
        Optional<StudyGroup> existingOpt = studyGroups.stream()
                .filter(group -> group.getId() == id)
                .findFirst();

        if (existingOpt.isPresent()) {
            StudyGroup existingGroup = existingOpt.get();
            try {
                StudyGroup updatedGroup = new StudyGroup(
                        existingGroup.getId(), updatedData.getName(), updatedData.getCoordinates(),
                        existingGroup.getCreationDate(), // Keep original date
                        updatedData.getStudentsCount(), updatedData.getShouldBeExpelled(),
                        updatedData.getFormOfEducation(), updatedData.getSemesterEnum(),
                        updatedData.getGroupAdmin());

                // Standard Set update: remove old, add new
                studyGroups.remove(existingGroup); // remove uses equals() based on ID
                studyGroups.add(updatedGroup);
                console.println("Element ID " + id + " updated.");
                return true;
            } catch (IllegalArgumentException e) {
                console.printError("Update failed for ID " + id + " (validation): " + e.getMessage());
                // Add the original back? Or assume client will retry? Let's keep it removed for now.
                return false;
            }
        } else {
            // console.printError("Update failed: Element with ID " + id + " not found."); // RequestHandler handles response
            return false; // Indicate not found
        }
    }

    /** Removes an element by ID using Stream API's removeIf. */
    public boolean removeById(int id) {
        boolean removed = studyGroups.removeIf(group -> group.getId() == id);
        if (removed) {
            console.println("Element ID " + id + " removed by removeById.");
        }
        return removed;
    }

    /** Clears the collection. */
    public void clearCollection() {
        studyGroups.clear();
        console.println("Collection cleared.");
    }

    /** Saves the collection using FileManager. */
    public void saveCollection() {
        fileManager.saveCollection(studyGroups);
    }

    /** Adds element if it's smaller than the minimum (natural order - ID). */
    public boolean addIfMin(StudyGroup candidateData) {
        // Find min using Stream API
        Optional<StudyGroup> minElementOpt = studyGroups.stream()
                .min(StudyGroup::compareTo);


        try {
            StudyGroup conceptualCandidate = new StudyGroup(
                    1, candidateData.getName(), candidateData.getCoordinates(), // Dummy ID
                    candidateData.getStudentsCount(), candidateData.getShouldBeExpelled(),
                    candidateData.getFormOfEducation(), candidateData.getSemesterEnum(),
                    candidateData.getGroupAdmin());

            if (minElementOpt.isEmpty() || conceptualCandidate.compareTo(minElementOpt.get()) < 0) {

                console.println("addIfMin: Condition met. Adding element.");
                addElement(candidateData); // Use the actual addElement method
                return true;
            } else {
                console.println("addIfMin: Condition not met (element not smaller or collection not empty).");
                return false;
            }
        } catch (IllegalArgumentException e) {
            console.printError("addIfMin: Cannot process candidate due to validation error: " + e.getMessage());
            return false;
        }
    }

    /** Removes elements smaller than the threshold (natural order - ID) using Stream API. */
    public int removeLower(StudyGroup thresholdData) {
        try {
            // Create conceptual threshold (ID doesn't matter here)
            StudyGroup threshold = new StudyGroup(
                    1, thresholdData.getName(), thresholdData.getCoordinates(), // Dummy ID
                    thresholdData.getStudentsCount(), thresholdData.getShouldBeExpelled(),
                    thresholdData.getFormOfEducation(), thresholdData.getSemesterEnum(),
                    thresholdData.getGroupAdmin());

            // Use removeIf with lambda
            int initialSize = studyGroups.size();
            boolean removed = studyGroups.removeIf(group -> group.compareTo(threshold) < 0);
            int removedCount = initialSize - studyGroups.size();

            console.println("removeLower: Removed " + removedCount + " elements.");
            return removedCount;

        } catch (IllegalArgumentException e) {
            console.printError("removeLower: Cannot process threshold due to validation error: " + e.getMessage());
            return 0;
        }
    }

    /** Removes one element matching the FormOfEducation using Stream API. */
    public boolean removeAnyByFormOfEducation(FormOfEducation form) {
        // Find first matching element
        Optional<StudyGroup> toRemoveOpt = studyGroups.stream()
                .filter(group -> group.getFormOfEducation() == form)
                .findFirst();

        if (toRemoveOpt.isPresent()) {
            studyGroups.remove(toRemoveOpt.get()); // Remove the found element
            console.println("Removed one element with form: " + form);
            return true;
        } else {
            console.println("No element found with form: " + form);
            return false;
        }
    }

    // --- Methods returning Lists (used by RequestHandler) ---

    /** Gets a list of elements sorted by natural order (ID) using Stream API. */
    public List<StudyGroup> getSortedElements() {
        return studyGroups.stream()
                .sorted() // Uses StudyGroup::compareTo (by ID)
                .collect(Collectors.toList());
    }

    /** Gets a list of elements sorted by student count using Stream API. */
    public List<StudyGroup> getElementsByStudentCount() {
        return studyGroups.stream()
                .sorted(Comparator.comparingLong(StudyGroup::getStudentsCount)) // Sort by specific field
                .collect(Collectors.toList());
    }

    /** Gets a list of group admins sorted by natural order (name) using Stream API. */
    public List<Person> getSortedGroupAdmins() {
        return studyGroups.stream()
                .map(StudyGroup::getGroupAdmin)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }
}