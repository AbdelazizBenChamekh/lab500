package org.example.server.core;

import org.example.common.models.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CollectionManager {
    private LinkedHashSet<StudyGroup> studyGroups;
    private final FileManager fileManager;
    private final LocalDateTime initializationTime;
    private final Logger logger;

    public CollectionManager(FileManager fileManager, Logger logger) {
        this.fileManager = fileManager;
        this.logger = logger;
        this.initializationTime = LocalDateTime.now();
        if (this.logger != null) logger.info("CollectionManager initializing...");
        this.studyGroups = fileManager.loadCollection();
        if (this.logger != null && this.studyGroups != null) logger.info("CollectionManager loaded " + this.studyGroups.size() + " elements.");
    }

    public LinkedHashSet<StudyGroup> getCollection() {
        return studyGroups;
    }

    public String getInfo() {
        return "Collection Type: LinkedHashSet<StudyGroup>\n" +
                "Initialization Date: " + initializationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                "Number of Elements: " + studyGroups.size();
    }

    public void addElement(StudyGroup groupData) {
        try {
            int newId = IdGenerator.generateId();
            StudyGroup newGroup = new StudyGroup(
                    newId, groupData.getName(), groupData.getCoordinates(),
                    groupData.getStudentsCount(), groupData.getShouldBeExpelled(),
                    groupData.getFormOfEducation(), groupData.getSemesterEnum(),
                    groupData.getGroupAdmin()
            );
            if (studyGroups.add(newGroup)) {
                if (logger != null) logger.info("Added element ID: " + newId + " to collection. New size: " + studyGroups.size());
            } else {
                if (logger != null) logger.warning("Element with ID " + newId + " could not be added (likely duplicate). Size: " + studyGroups.size());
            }
        } catch (IllegalArgumentException e) {
            if (logger != null) logger.log(Level.SEVERE, "Failed to add element due to invalid data: " + e.getMessage(), e);
        }
    }

    public StudyGroup findById(int id) {
        return studyGroups.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
    }

    public boolean updateElement(int id, StudyGroup updatedDataFromClient) {
        StudyGroup existingGroup = findById(id);
        if (existingGroup == null) {
            if (logger != null) logger.warning("Update failed: Element with ID " + id + " not found.");
            return false;
        }
        try {
            StudyGroup updatedGroup = new StudyGroup(
                    existingGroup.getId(),
                    updatedDataFromClient.getName(),
                    updatedDataFromClient.getCoordinates(),
                    existingGroup.getCreationDate(),
                    updatedDataFromClient.getStudentsCount(),
                    updatedDataFromClient.getShouldBeExpelled(),
                    updatedDataFromClient.getFormOfEducation(),
                    updatedDataFromClient.getSemesterEnum(),
                    updatedDataFromClient.getGroupAdmin()
            );
            studyGroups.remove(existingGroup);
            studyGroups.add(updatedGroup);
            if (logger != null) logger.info("Element ID " + id + " updated successfully.");
            return true;
        } catch (IllegalArgumentException e) {
            if (logger != null) logger.log(Level.SEVERE, "Update failed for ID " + id + ": Invalid data. " + e.getMessage(), e);
            return false;
        }
    }

    public boolean removeById(int id) {
        boolean removed = studyGroups.removeIf(group -> group.getId() == id);
        if (removed && logger != null) logger.info("Element ID " + id + " removed. New size: " + studyGroups.size());
        else if (!removed && logger != null) logger.warning("Remove failed: Element ID " + id + " not found.");
        return removed;
    }

    public void clearCollection() {
        studyGroups.clear();
        if (logger != null) logger.info("Collection cleared. New size: 0");
    }

    public void saveCollection() {
        if (logger != null) logger.info("Saving collection to file via FileManager...");
        fileManager.saveCollection(this.studyGroups);
    }

    public List<StudyGroup> getAllGroupsSortedBySize() {
        return this.studyGroups.stream()
                .sorted(Comparator.comparingLong(StudyGroup::getStudentsCount))
                .collect(Collectors.toList());
    }

    public List<StudyGroup> getAllGroupsSortedById() {
        return this.studyGroups.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Person> getSortedGroupAdmins() {
        return studyGroups.stream()
                .map(StudyGroup::getGroupAdmin)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    public long removeLower(StudyGroup threshold) {
        long initialSize = studyGroups.size();
        studyGroups = studyGroups.stream()
                .filter(group -> group.compareTo(threshold) >= 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        long removedCount = initialSize - studyGroups.size();
        if (logger != null) logger.info("remove_lower executed. Removed " + removedCount + " elements. New size: " + studyGroups.size());
        return removedCount;
    }

    public boolean addIfMin(StudyGroup candidate) {
        Optional<StudyGroup> minElementOpt = studyGroups.stream().min(StudyGroup::compareTo);
        if (minElementOpt.isEmpty()) {
            addElement(candidate);
            if (logger != null) logger.info("add_if_min: Collection was empty, element added.");
            return true;
        } else {
            if (logger != null) logger.info("add_if_min: Element not added (not smaller than min or collection not empty). Candidate ID would be new, min ID: " + minElementOpt.get().getId());
            return false;
        }
    }

    public boolean removeAnyByFormOfEducation(FormOfEducation form) {
        Optional<StudyGroup> toRemove = studyGroups.stream()
                .filter(group -> group.getFormOfEducation() == form)
                .findFirst();
        if (toRemove.isPresent()) {
            studyGroups.remove(toRemove.get());
            if (logger != null) logger.info("remove_any_by_form_of_education: Removed one element with form " + form + ". New size: " + studyGroups.size());
            return true;
        }
        if (logger != null) logger.info("remove_any_by_form_of_education: No element found with form " + form);
        return false;
    }
}