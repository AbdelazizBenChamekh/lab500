package org.example.server.core;

import org.example.common.models.FormOfEducation;
import org.example.common.models.Person;
import org.example.server.exceptions.InvalidForm;
import org.example.common.models.StudyGroup;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class that manages the collection.
 */
public class CollectionManager {
    private final ArrayDeque<StudyGroup> collection = new ArrayDeque<>();
    private static int nextId = 0;
    /**
     * Collection creation date
     */
    private LocalDateTime lastInitTime;
    /**
     * Last modification date of the collection
     */
    private LocalDateTime lastSaveTime;
    private static final Logger collectionManagerLogger = Logger.getLogger(CollectionManager.class.getName());

    public CollectionManager() {
        this.lastInitTime = LocalDateTime.now();
        this.lastSaveTime = null;
    }

    public ArrayDeque<StudyGroup> getCollection() {
        return collection;
    }

    public static void updateId(Collection<StudyGroup> collection) {
        nextId = collection.stream()
                .filter(Objects::nonNull)
                .map(StudyGroup::getId)
                .max(Integer::compareTo)
                .orElse(0);
        collectionManagerLogger.info("ID updated to " + nextId);
    }

    public static int getNextId() {
        return ++nextId;
    }

    /**
     * Formats the date, hiding the date if it's today
     * @param localDateTime LocalDateTime object
     * @return formatted date string
     */
    public static String timeFormatter(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        if (localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .equals(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
            return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Formats the date, hiding the date if it's today
     * @param dateToConvert Date object
     * @return formatted date string
     */
    public static String timeFormatter(Date dateToConvert) {
        LocalDateTime localDateTime = dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        if (localDateTime == null) return null;
        if (localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .equals(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
            return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getLastInitTime() {
        return timeFormatter(lastInitTime);
    }

    public String getLastSaveTime() {
        return timeFormatter(lastSaveTime);
    }

    /**
     * @return The type name of the collection.
     */
    public String collectionType() {
        return collection.getClass().getName();
    }

    public int collectionSize() {
        return collection.size();
    }

    public void clear() {
        this.collection.clear();
        lastInitTime = LocalDateTime.now();
        collectionManagerLogger.info("Collection cleared");
    }

    public StudyGroup getLast() {
        return collection.getLast();
    }

    /**
     * @param id Element ID.
     * @return The element by its ID, or null if not found.
     */
    public StudyGroup getById(int id) {
        for (StudyGroup element : collection) {
            if (element.getId() == id) return element;
        }
        return null;
    }

    /**
     * Edit the collection element with the given id
     * @param id id
     * @param newElement new element
     * @throws InvalidForm No element with such id
     */
    public void editById(int id, StudyGroup newElement) {
        StudyGroup pastElement = this.getById(id);
        this.removeElement(pastElement);
        newElement.setId(id);
        this.addElement(newElement);
        collectionManagerLogger.info("Object with id " + id + " edited: " + newElement);
    }

    /**
     * @param id Element ID.
     * @return Checks if an element with such ID exists.
     */
    public boolean checkExist(int id) {
        return collection.stream()
                .anyMatch((x) -> x.getId() == id);
    }

    /**
     * Adds the given StudyGroup to the collection if it is less than the current minimum element.
     * @param candidate the StudyGroup to add
     * @return true if the element was added, false otherwise
     * @throws IllegalArgumentException if candidate is null
     */
    public boolean addIfMin(StudyGroup candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate StudyGroup cannot be null");
        }
        if (collection.isEmpty()) {
            addElement(candidate);
            return true;
        }
        StudyGroup minElement = collection.stream()
                .min(StudyGroup::compareTo)  // assumes StudyGroup implements Comparable<StudyGroup>
                .orElse(null);
        if (minElement == null || candidate.compareTo(minElement) < 0) {
            addElement(candidate);
            return true;
        }
        return false;
    }

    /**
     * Removes all elements smaller than the given threshold.
     * @param threshold the StudyGroup threshold
     * @return the number of elements removed
     * @throws IllegalArgumentException if threshold is null
     */
    public long removeLower(StudyGroup threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("Threshold StudyGroup cannot be null");
        }
        long initialSize = collection.size();
        collection.removeIf(element -> element.compareTo(threshold) < 0);
        long removedCount = initialSize - collection.size();
        collectionManagerLogger.info("Removed " + removedCount + " elements smaller than threshold: " + threshold);
        lastSaveTime = LocalDateTime.now();
        return removedCount;
    }

    public void addElement(StudyGroup studyGroup) {
        this.lastSaveTime = LocalDateTime.now();
        collection.add(studyGroup);
        collectionManagerLogger.info("Object added to collection: " + studyGroup);
    }

    public void removeElement(StudyGroup studyGroup) {
        collection.remove(studyGroup);
    }

    public List<StudyGroup> getAllGroupsSortedById() {
        return collection.stream()
                .sorted(Comparator.comparingInt(StudyGroup::getId))
                .collect(Collectors.toList());
    }

    // new method here!
    /**
     * @return all elements sorted by size
     */
    public List<StudyGroup> getAllGroupsSortedBySize() {
        return collection.stream()
                .sorted(Comparator.comparingLong(StudyGroup::getStudentsCount))
                .collect(Collectors.toList());
    }

    public boolean removeAnyByFormOfEducation(FormOfEducation form) {
        Optional<StudyGroup> toRemove = collection.stream()
                .filter(sg -> sg.getFormOfEducation() == form)
                .findFirst();
        if (toRemove.isPresent()) {
            collection.remove(toRemove.get());
            return true;
        }
        return false;
    }

    public List<Person> getSortedGroupAdmins() {
        return collection.stream()
                .map(StudyGroup::getGroupAdmin)
                .sorted(Comparator.comparing(Person::getName)) // or another field if needed
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        if (collection.isEmpty()) return "The collection is empty!";
        var last = getLast();
        StringBuilder info = new StringBuilder();
        for (StudyGroup studyGroup : collection) {
            info.append(studyGroup);
            if (studyGroup != last) info.append("\n\n");
        }
        return info.toString();
    }
}
