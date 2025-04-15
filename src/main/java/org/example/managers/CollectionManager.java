package org.example.managers;

import org.example.models.FormOfEducation;
import org.example.models.Person;
import org.example.models.StudyGroup;
import org.example.utility.ConsoleReader;
import org.example.managers.IdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the main collection of StudyGroup objects (LinkedHashSet).
 * Handles operations like add, update, remove, clear, info, etc.
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
    }

    /** Returns the collection instance. */
    public LinkedHashSet<StudyGroup> getCollection() {
        return studyGroups;
    }

    /** Gets basic info about the collection. */
    public String getInfo() {
        return "Collection Type: LinkedHashSet<StudyGroup>\n" +
                "Initialization Date: " + initializationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                "Number of Elements: " + studyGroups.size();
    }

    /** Adds a new element with a generated ID. */
    public void addElement(StudyGroup groupData) {
        try {
            int newId = IdGenerator.generateId();
            // Use the constructor that sets creationDate automatically
            StudyGroup newGroup = new StudyGroup(
                    newId,
                    groupData.getName(), groupData.getCoordinates(),
                    groupData.getStudentsCount(), groupData.getShouldBeExpelled(),
                    groupData.getFormOfEducation(), groupData.getSemesterEnum(),
                    groupData.getGroupAdmin()
            );
            studyGroups.add(newGroup);
            console.println("New element added with ID: " + newId);
        } catch (IllegalArgumentException e) {
            console.printError("Failed to add element: " + e.getMessage());
        }
    }

    /** Finds an element by ID. Returns null if not found. */
    public StudyGroup findById(int id) {
        for (StudyGroup group : studyGroups) {
            if (group.getId() == id) {
                return group;
            }
        }
        return null;
    }

    /** Updates an existing element, preserving ID and creation date. */
    public boolean updateElement(int id, StudyGroup updatedData) {
        StudyGroup existingGroup = findById(id);
        if (existingGroup == null) {
            console.printError("Update failed: Element with ID " + id + " not found.");
            return false;
        }

        try {
            // Create the fully updated object using the loading constructor
            StudyGroup updatedGroup = new StudyGroup(
                    existingGroup.getId(),
                    updatedData.getName(),
                    updatedData.getCoordinates(),
                    existingGroup.getCreationDate(),
                    updatedData.getStudentsCount(),
                    updatedData.getShouldBeExpelled(),
                    updatedData.getFormOfEducation(),
                    updatedData.getSemesterEnum(),
                    updatedData.getGroupAdmin()
            );

            // Remove old, add new.
            studyGroups.remove(existingGroup);
            studyGroups.add(updatedGroup);
            console.println("Element ID " + id + " updated.");
            return true;
        } catch (IllegalArgumentException e) {
            console.printError("Update failed for ID " + id + ": Invalid data provided. " + e.getMessage());
            // If update fails, the original element might still be in the set or removed.
            return false;
        }
    }

    /** Removes an element by ID. */
    public boolean removeById(int id) {
        StudyGroup groupToRemove = findById(id);
        if (groupToRemove != null) {
            studyGroups.remove(groupToRemove);
            return true;
        }
        return false;
    }

    /** Clears the collection. */
    public void clearCollection() {
        studyGroups.clear();
    }

    /** Saves the collection using FileManager. */
    public void saveCollection() {
        fileManager.saveCollection(studyGroups);
    }

    /** Adds element if it's smaller than the minimum (based on ID). Only adds if collection is empty. */
    public boolean addIfMin(StudyGroup candidateData) {
        StudyGroup minElement = null;
        if (!studyGroups.isEmpty()) {
            // Find minimum using iteration
            minElement = studyGroups.iterator().next();
            for (StudyGroup group : studyGroups) {
                if (group.compareTo(minElement) < 0) {
                    minElement = group;
                }
            }
        }
        // Since new IDs are always increasing, a new element can only be added if the collection is empty.
        if (minElement == null) { // Collection was empty
            console.println("Collection empty. Adding element.");
            addElement(candidateData);
            return true;
        } else {
            console.println("Element not added: New elements always have higher IDs than the minimum (ID: " + minElement.getId() + ").");
            return false;
        }
    }

    /** Removes elements smaller than the threshold (based on ID). */
    public int removeLower(StudyGroup thresholdData) {
        int initialSize = studyGroups.size();
        // Create a conceptual threshold object (ID doesn't matter for comparison logic)
        try {
            StudyGroup threshold = new StudyGroup(
                    1, thresholdData.getName(), thresholdData.getCoordinates(),
                    thresholdData.getStudentsCount(), thresholdData.getShouldBeExpelled(),
                    thresholdData.getFormOfEducation(), thresholdData.getSemesterEnum(),
                    thresholdData.getGroupAdmin()
            );

            // Use removeIf with lambda (common and relatively clear)
            studyGroups.removeIf(group -> group.compareTo(threshold) < 0);

            int removedCount = initialSize - studyGroups.size();
            if (removedCount > 0) {
                console.println("Removed " + removedCount + " elements.");
            } else {
                console.println("No elements smaller than the threshold found.");
            }
            return removedCount;

        } catch (IllegalArgumentException e) {
            console.printError("Cannot perform remove_lower: Invalid data for threshold element. " + e.getMessage());
            return 0;
        }
    }

    /** Removes one element matching the FormOfEducation. */
    public boolean removeAnyByFormOfEducation(FormOfEducation form) {
        StudyGroup toRemove = null;
        for (StudyGroup group : studyGroups) {
            if (group.getFormOfEducation() == form) {
                toRemove = group;
                break;
            }
        }
        if (toRemove != null) {
            studyGroups.remove(toRemove);
            return true;
        }
        return false;
    }

    /** Gets a sorted list of elements (by ID). */
    public List<StudyGroup> getSortedElements() {
        List<StudyGroup> sortedList = new ArrayList<>(studyGroups);
        Collections.sort(sortedList); // Sorts based on StudyGroup.compareTo (ID)
        return sortedList;
    }

    /** Gets a sorted list of group admins (by name). */
    public List<Person> getSortedGroupAdmins() {
        List<Person> adminList = new ArrayList<>();
        for (StudyGroup group : studyGroups) {
            if (group.getGroupAdmin() != null) { // Should always be true due to validation
                adminList.add(group.getGroupAdmin());
            }
        }
        Collections.sort(adminList);
        return adminList;
    }
}