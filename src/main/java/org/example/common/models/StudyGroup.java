package org.example.common.models;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a study group. Implements Comparable for sorting by ID.
 * Contains validation logic and CSV serialization helpers.
 */
public class StudyGroup implements Comparable<StudyGroup>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private int id; // > 0, Unique, Auto-generated
    private String name; // Not null, Not empty
    private Coordinates coordinates; // Not null
    private java.time.LocalDate creationDate; // Not null, Auto-generated
    private long studentsCount; // > 0
    private Long shouldBeExpelled; // > 0, Can be null
    private FormOfEducation formOfEducation; // Not null
    private Semester semesterEnum; // Can be null
    private Person groupAdmin; // Not null

    /**
     * Constructor for NEW groups (ID provided, Date generated now).
     * Validates all fields.
     */
    public StudyGroup(int id, String name, Coordinates coordinates, long studentsCount, Long shouldBeExpelled, FormOfEducation formOfEducation, Semester semesterEnum, Person groupAdmin) {
        // ID check
        if (id <= 0)
            throw new IllegalArgumentException("Internal Error: StudyGroup ID must be > 0.");
        this.id = id;


        setName(name);
        setCoordinates(coordinates);
        setStudentsCount(studentsCount);
        setShouldBeExpelled(shouldBeExpelled);
        setFormOfEducation(formOfEducation);
        setSemesterEnum(semesterEnum);
        setGroupAdmin(groupAdmin);


        this.creationDate = LocalDate.now();
    }

    /**
     * Constructor for loading/updating groups (ID and Date provided).
     * Validates all fields.
     */
    public StudyGroup(int id, String name, Coordinates coordinates, LocalDate creationDate, long studentsCount, Long shouldBeExpelled, FormOfEducation formOfEducation, Semester semesterEnum, Person groupAdmin) {

        if (id <= 0)
            throw new IllegalArgumentException("Loaded StudyGroup ID must be > 0.");
        this.id = id;


        if (creationDate == null) {
            throw new IllegalArgumentException("Loaded StudyGroup creationDate cannot be null.");
        }
        this.creationDate = creationDate;

        setName(name);
        setCoordinates(coordinates);
        setStudentsCount(studentsCount);
        setShouldBeExpelled(shouldBeExpelled);
        setFormOfEducation(formOfEducation);
        setSemesterEnum(semesterEnum);
        setGroupAdmin(groupAdmin);
    }


    public int getId() {
        return id; }
    public String getName() {
        return name; }
    public Coordinates getCoordinates() {
        return coordinates; }
    public LocalDate getCreationDate() {
        return creationDate; }
    public long getStudentsCount() {
        return studentsCount; }
    public Long getShouldBeExpelled() {
        return shouldBeExpelled; }
    public FormOfEducation getFormOfEducation() {
        return formOfEducation; }
    public Semester getSemesterEnum() {
        return semesterEnum; }
    public Person getGroupAdmin() {
        return groupAdmin; }


    private void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("StudyGroup name cannot be null or empty.");
        }
        this.name = name.trim();
    }
    private void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("StudyGroup coordinates cannot be null.");
        }
        this.coordinates = coordinates;
    }
    private void setStudentsCount(long studentsCount) {
        if (studentsCount <= 0) {
            throw new IllegalArgumentException("StudyGroup studentsCount must be > 0. Received: " + studentsCount);
        }
        this.studentsCount = studentsCount;
    }
    private void setShouldBeExpelled(Long shouldBeExpelled) {
        if (shouldBeExpelled != null && shouldBeExpelled <= 0) {
            throw new IllegalArgumentException("StudyGroup shouldBeExpelled must be > 0 if provided. Received: " + shouldBeExpelled);
        }
        this.shouldBeExpelled = shouldBeExpelled;
    }
    private void setFormOfEducation(FormOfEducation formOfEducation) {
        if (formOfEducation == null) {
            throw new IllegalArgumentException("StudyGroup formOfEducation cannot be null.");
        }
        this.formOfEducation = formOfEducation;
    }
    private void setSemesterEnum(Semester semesterEnum) {
        this.semesterEnum = semesterEnum;
    }
    private void setGroupAdmin(Person groupAdmin) {
        if (groupAdmin == null) {
            throw new IllegalArgumentException("StudyGroup groupAdmin cannot be null.");
        }
        this.groupAdmin = groupAdmin;
    }

    /** Compares StudyGroups by ID. */
    @Override
    public int compareTo(StudyGroup other) {
        return Integer.compare(this.id, other.id);
    }

    /** Checks equality based ONLY on ID. */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        StudyGroup that = (StudyGroup) o;
        return id == that.id;
    }

    /** Hash code based ONLY on ID. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StudyGroup [ID=" + id + ", Name='" + name + "', Coords=" + coordinates +
                ", Created=" + creationDate + ", Count=" + studentsCount +
                ", Expelled=" + (shouldBeExpelled == null ? "N/A" : shouldBeExpelled) +
                ", Form=" + formOfEducation +
                ", Semester=" + (semesterEnum == null ? "N/A" : semesterEnum) +
                ", Admin=" + groupAdmin + "]";
    }

    /** Converts the object to a simple CSV string.
     * Used later by FileManager to write data to data.csv.
     */
    public String toCsv() {
        // Creating a String variable to hold text representation.
        String shouldBeExpelledStr = (shouldBeExpelled == null) ? "" : String.valueOf(shouldBeExpelled);
        String semesterStr = (semesterEnum == null) ? "" : semesterEnum.name();
        String eyeColorStr = (groupAdmin.getEyeColor() == null) ? "" : groupAdmin.getEyeColor().name();
        String nationalityStr = (groupAdmin.getNationality() == null) ? "" : groupAdmin.getNationality().name();
        String locNameStr = (groupAdmin.getLocation().getName() == null) ? "" : groupAdmin.getLocation().getName();

        // Simple comma separation.
        return String.join(",",
                String.valueOf(id),
                name,
                String.valueOf(coordinates.getX()),
                String.valueOf(coordinates.getY()),
                creationDate.toString(),
                String.valueOf(studentsCount),
                shouldBeExpelledStr,
                formOfEducation.name(),
                semesterStr,
                groupAdmin.getName(),
                String.valueOf(groupAdmin.getWeight()),
                eyeColorStr,
                groupAdmin.getHairColor().name(),
                nationalityStr,
                String.valueOf(groupAdmin.getLocation().getX()),
                String.valueOf(groupAdmin.getLocation().getY()),
                locNameStr
        );
    }
}