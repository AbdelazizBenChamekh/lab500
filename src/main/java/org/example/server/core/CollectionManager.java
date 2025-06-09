package org.example.server.core;

import org.example.common.models.FormOfEducation;
import org.example.common.models.Person;
import org.example.common.models.StudyGroup;
import org.example.common.network.User;
import org.example.server.exceptions.InvalidForm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CollectionManager {
    private final ArrayDeque<StudyGroup> collection = new ArrayDeque<>();
    private DatabaseManager databaseManager;
    private final ConcurrentHashMap<Integer, StudyGroup> collectionCache;
    private final LocalDateTime initializationTime;
    LocalDateTime lastInitTime;
    private Logger logger;
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    Lock writeLock = lock.writeLock();
    Lock readLock = lock.readLock();
    private LocalDateTime lastSaveTime;

    public CollectionManager(DatabaseManager databaseManager) {
        this.logger = Logger.getLogger(CollectionManager.class.getName());
        this.databaseManager = databaseManager;
        this.initializationTime = LocalDateTime.now();
        this.collectionCache = new ConcurrentHashMap<>();
        logger.info("CollectionManager initialized. Initial cache size: " + this.collectionCache.size());
    }

    public String getInfo() {
        String initTimeFormatted = (initializationTime != null)
                ? initializationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "N/A";

        return "Server Collection Information:\n" +
                "  In-Memory Cache Type: " + collectionCache.getClass().getSimpleName() + "\n" +
                "  Elements in Cache: " + collectionCache.size() + "\n" +
                "  Cache Initialization Time: " + initTimeFormatted + "\n" +
                "  Primary Data Store: PostgreSQL Database\n" +
                "  Note: Cache is updated upon successful database modifications.";
    }
    public ArrayDeque<StudyGroup> getCollection() {
        try {
            readLock.lock();
            return collection;
        } finally {
            readLock.unlock();
        }
    }

    public void addElement(StudyGroup studyGroup) throws InvalidForm{
        this.lastSaveTime = LocalDateTime.now();
        if (!studyGroup.validate()) throw new InvalidForm("Количество студентов должно быть положительным");
        collection.add(studyGroup);
    }


    public void removeElements(Collection<StudyGroup> collection) {
        try {
            writeLock.lock();
            this.collection.removeAll(collection);
        } finally {
            writeLock.unlock();
        }
    }

    public List<StudyGroup> getAllGroupsSortedById() {
        logger.fine("CM: Getting all groups sorted by ID from local cache.");
        if (collectionCache.isEmpty()) return Collections.emptyList();
        return collectionCache.values().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public StudyGroup getById(int id) {
        logger.fine("CM: Getting group by ID " + id + " from local cache.");
        return collectionCache.get(id);
    }

    public List<Person> getSortedGroupAdmins() {
        logger.fine("CM: Getting sorted group admins from local cache.");
        if (collectionCache.isEmpty()) return Collections.emptyList();
        return collectionCache.values().stream()
                .map(StudyGroup::getGroupAdmin)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());
    }

    public boolean addIfMin(StudyGroup candidate, User user) throws InvalidForm {
        logger.info("CM: addIfMin called by user " + user.getLogin());

        // Validate candidate first
        if (!candidate.validate()) {
            throw new InvalidForm("StudyGroup validation failed.");
        }

        writeLock.lock();
        try {
            if (collectionCache.isEmpty()) {
                // No elements yet, just add
                candidate.setOwnerLogin(String.valueOf(user.getLogin()));  // Set ownership
                int generatedId = databaseManager.addObject(candidate, user.getLogin());
                if (generatedId != -1) {
                    candidate.setId(generatedId);
                    collectionCache.put(generatedId, candidate);
                    collection.add(candidate);
                    logger.info("CM: addIfMin - Added first element to empty collection for user " + user.getLogin());
                    return true;
                } else {
                    logger.warning("CM: addIfMin - DB insert failed for user " + user.getLogin());
                    return false;
                }
            } else {
                // Find current minimum element in cache
                Optional<StudyGroup> minElementOpt = collectionCache.values().stream()
                        .min(StudyGroup::compareTo);

                if (minElementOpt.isPresent() && candidate.compareTo(minElementOpt.get()) < 0) {
                    candidate.setOwnerLogin(String.valueOf(user.getLogin()));  // Set ownership
                    int generatedId = databaseManager.addObject(candidate, user.getLogin());
                    if (generatedId != -1) {
                        candidate.setId(generatedId);
                        collectionCache.put(generatedId, candidate);
                        collection.add(candidate);
                        logger.info("CM: addIfMin - Candidate added as it is less than current min for user " + user.getLogin());
                        return true;
                    } else {
                        logger.warning("CM: addIfMin - DB insert failed for user " + user.getLogin());
                        return false;
                    }
                } else {
                    logger.info("CM: addIfMin - Candidate not less than current min for user " + user.getLogin());
                    return false;
                }
            }
        } finally {
            writeLock.unlock();
        }
    }


    public long removeLower(StudyGroup thresholdFromClient, User user) {
        logger.info("CM: RemoveLower for user " + user.getLogin());
        logger.warning("CM: RemoveLower - Current implementation is cache-first and needs DB-level atomic operation with ownership.");

        List<StudyGroup> toRemoveFromCache = collectionCache.values().stream()
                .filter(group -> group.getOwnerLogin() != null && group.getOwnerLogin().equals(user.getLogin()))
                .filter(group -> group.compareTo(thresholdFromClient) < 0)
                .collect(Collectors.toList());

        if (toRemoveFromCache.isEmpty()) {
            logger.info("CM: RemoveLower - No elements owned by user " + user.getLogin() + " found smaller than threshold in cache.");
            return 0;
        }

        long removedCountDB = 0;
        for (StudyGroup group : toRemoveFromCache) {
            if (databaseManager.deleteObject(group.getId(), user.getLogin())) {
                collectionCache.remove(group.getId());
                removedCountDB++;
            } else {
                logger.warning("CM: RemoveLower - Failed to delete group ID " + group.getId() + " from DB (not owned or other DB error).");

            }
        }
        logger.info("CM: RemoveLower - Successfully removed " + removedCountDB + " elements from DB and cache for user " + user.getLogin());
        return removedCountDB;
    }

    public boolean checkExist(int id) {
        try {
            readLock.lock();
            return collection.stream()
                    .anyMatch((x) -> x.getId() == id);
        } finally {
            readLock.unlock();
        }
    }

    public static String timeFormatter(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        if (localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .equals(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
            return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        }
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getLastInitTime() {
        try {
            readLock.lock();
            return timeFormatter(lastInitTime);
        } finally {
            readLock.unlock();
        }
    }

    public void editById(int id, StudyGroup newElement) {
        try {
            writeLock.lock();
            StudyGroup pastElement = this.getById(id);
            this.removeElement(pastElement);
            newElement.setId(id);
            this.addElement(newElement);
            logger.info("Объект с айди " + id + " изменен");
        } catch (InvalidForm e) {
            throw new RuntimeException(e);
        } finally {
            writeLock.unlock();
        }
    }

    public String collectionType() {
        try {
            readLock.lock();
            return collection.getClass().getName();
        } finally {
            readLock.unlock();
        }
    }

    public int collectionSize() {
        try {
            readLock.lock();
            return collection.size();
        } finally {
            readLock.unlock();
        }
    }

    public String getLastSaveTime() {
        try {
            readLock.lock();
            return timeFormatter(lastSaveTime);
        } finally {
            readLock.unlock();
        }
    }

    public boolean removeAnyByFormOfEducation(FormOfEducation form, User user) {
        logger.info("CM: RemoveAnyByForm for user " + user.getLogin() + " and form " + form);
        Optional<StudyGroup> candidateToRemoveOpt = collectionCache.values().stream()
                .filter(sg -> sg.getOwnerLogin() != null && sg.getOwnerLogin().equals(user.getLogin()))
                .filter(sg -> sg.getFormOfEducation() == form)
                .findFirst();

        if (candidateToRemoveOpt.isPresent()) {
            StudyGroup groupToRemove = candidateToRemoveOpt.get();
            logger.info("CM: RemoveAnyByForm - Candidate ID " + groupToRemove.getId() + " found in cache. Attempting DB delete.");
            boolean success = databaseManager.deleteObject(groupToRemove.getId(), user.getLogin());
            if (success) {
                collectionCache.remove(groupToRemove.getId());
                logger.info("CM: RemoveAnyByForm - Successfully removed group ID " + groupToRemove.getId() + " from DB and cache.");
                return true;
            } else {
                logger.warning("CM: RemoveAnyByForm - DB delete failed for group ID " + groupToRemove.getId());
                return false;
            }
        } else {
            logger.info("CM: RemoveAnyByForm - No element with form " + form + " owned by user " + user.getLogin() + " found in cache.");
            return false;
        }
    }

    public void removeElement(StudyGroup studyGroup) {
        try {
            writeLock.lock();
            collection.remove(studyGroup);
        } finally {
            writeLock.unlock();
        }
    }
}
