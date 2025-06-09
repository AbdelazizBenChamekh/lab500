package org.example.common.models;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a study group. Implements Comparable for sorting by ID.
 * Implements Serializable for network transfer and database interaction.
 * Validation is performed in constructors and setters, with an additional validate() method.
 */
public class StudyGroup implements Validator, Comparable<StudyGroup>, Serializable { // REMOVED javax.xml.validation.Validator
    @Serial
    private static final long serialVersionUID = 701L;

    // Fields (as in your last version, with id and creationDate potentially final)
    private int id;                 // Can be 0 for client-side new, positive from DB
    private String name;
    private Coordinates coordinates;
    private LocalDate creationDate;   // Can be temp client-side, authoritative from server
    private long studentsCount;
    private Long shouldBeExpelled;
    private FormOfEducation formOfEducation;
    private Semester semesterEnum;
    private Person groupAdmin;
    private String ownerLogin;
    private String userLogin;

    // CLIENT-SIDE constructor (for preparing an object to send to server)
    public StudyGroup(
            String name,
            Coordinates coordinates,
            long studentsCount,
            Long shouldBeExpelled,
            FormOfEducation formOfEducation,
            Semester semesterEnum,
            Person groupAdmin
    ) {
        // Setters perform validation
        setName(name);
        setCoordinates(coordinates);
        setStudentsCount(studentsCount);
        setShouldBeExpelled(shouldBeExpelled);
        setFormOfEducation(formOfEducation);
        setSemesterEnum(semesterEnum);
        setGroupAdmin(groupAdmin);

        this.id = 0; // Client-side placeholder, indicates it's not yet persisted
        this.creationDate = LocalDate.now(); // Temporary, server will set the authoritative one
        this.ownerLogin = null; // Server will set this
    }

    // SERVER-SIDE constructor (from DB or after DB insert)
    public StudyGroup(
            int id,
            String name,
            Coordinates coordinates,
            LocalDate creationDate,
            long studentsCount,
            Long shouldBeExpelled,
            FormOfEducation formOfEducation,
            Semester semesterEnum,
            Person groupAdmin,
            String ownerLogin
    ) {
        // ID Validation (constructor specific)
        if (id <= 0) {
            throw new IllegalArgumentException("StudyGroup ID from DB must be > 0.");
        }
        this.id = id;

        // CreationDate Validation (constructor specific)
        if (creationDate == null) {
            throw new IllegalArgumentException("StudyGroup creationDate from DB cannot be null.");
        }
        this.creationDate = creationDate;

        // Use setters for other fields for their validation
        setName(name);
        setCoordinates(coordinates);
        setStudentsCount(studentsCount);
        setShouldBeExpelled(shouldBeExpelled);
        setFormOfEducation(formOfEducation);
        setSemesterEnum(semesterEnum);
        setGroupAdmin(groupAdmin);
        setOwnerLogin(ownerLogin);
    }

    // --- Getters (Unchanged) ---
    public int getId() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public long getStudentsCount() { return studentsCount; }
    public Long getShouldBeExpelled() { return shouldBeExpelled; }
    public FormOfEducation getFormOfEducation() { return formOfEducation; }
    public Semester getSemesterEnum() { return semesterEnum; }
    public Person getGroupAdmin() { return groupAdmin; }
    public String getOwnerLogin() { return ownerLogin; }
    public LocalDate getCreationDate() {
        return creationDate;
    }
    public String getUserLogin() {
        return userLogin;
    }

    // --- Setters (Mostly unchanged, ensure they throw IllegalArgumentException) ---
    // Public setters for ID, creationDate, ownerLogin are generally not recommended
    // if they are meant to be immutable after server-side finalization.
    // If needed for some reason (e.g., ORM frameworks sometimes require them), add them carefully.
    // For now, assuming they are set via constructor primarily.

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("StudyGroup name cannot be null or empty.");
        }
        this.name = name.trim();
    }
    // ... other setters from your previous version ...
    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("StudyGroup coordinates cannot be null.");
        }
        this.coordinates = coordinates;
    }

    public void setStudentsCount(long studentsCount) {
        if (studentsCount <= 0) {
            throw new IllegalArgumentException("StudyGroup studentsCount must be > 0. Received: " + studentsCount);
        }
        this.studentsCount = studentsCount;
    }

    public void setShouldBeExpelled(Long shouldBeExpelled) {
        if (shouldBeExpelled != null && shouldBeExpelled <= 0) {
            throw new IllegalArgumentException("StudyGroup shouldBeExpelled must be > 0 if provided. Received: " + shouldBeExpelled);
        }
        this.shouldBeExpelled = shouldBeExpelled;
    }

    public void setFormOfEducation(FormOfEducation formOfEducation) {
        if (formOfEducation == null) {
            throw new IllegalArgumentException("StudyGroup formOfEducation cannot be null.");
        }
        this.formOfEducation = formOfEducation;
    }

    public void setSemesterEnum(Semester semesterEnum) {
        this.semesterEnum = semesterEnum; // Nullable
    }


    public void setGroupAdmin(Person groupAdmin) {
        if (groupAdmin == null) {
            throw new IllegalArgumentException("StudyGroup groupAdmin cannot be null.");
        }
        this.groupAdmin = groupAdmin;
    }

    // Setter for ownerLogin, used by server-side constructor
    // Could be public if needed, but often server-set and then immutable.
    public void setOwnerLogin(String ownerLogin) {
        if (ownerLogin == null || ownerLogin.trim().isEmpty()) {
            // For an object freshly loaded from DB, ownerLogin SHOULD NOT be null.
            // For a client-side constructed object, it might be null before server assignment.
            // Let's make it strict for when it IS set.
            throw new IllegalArgumentException("Owner login, when set, cannot be null or empty.");
        }
        this.ownerLogin = ownerLogin.trim();
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }


    @Override
    public boolean validate() {
        if (id < 0) return false;
        if (name == null || name.trim().isEmpty()) return false;
        if (coordinates == null) return false;  // only check not null
        if (creationDate == null) return false;
        if (studentsCount <= 0) return false;
        if (shouldBeExpelled != null && shouldBeExpelled <= 0) return false;
        if (formOfEducation == null) return false;
        if (groupAdmin == null) return false;  // only check not null
        return true;
    }




    // --- compareTo, equals, hashCode, toString (Unchanged from your last version) ---
    @Override
    public int compareTo(StudyGroup other) {
        return Integer.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudyGroup that = (StudyGroup) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StudyGroup [ID=" + id +
                ", Name='" + name + '\'' +
                ", Owner='" + (ownerLogin == null ? "N/A_TEMP" : ownerLogin) + '\'' +
                ", Coords=" + coordinates +
                ", Created=" + (creationDate != null ? creationDate : "TEMP") +
                ", Count=" + studentsCount +
                ", Expelled=" + (shouldBeExpelled == null ? "N/A" : shouldBeExpelled) +
                ", Form=" + formOfEducation +
                ", Semester=" + (semesterEnum == null ? "N/A" : semesterEnum) +
                ", Admin=" + groupAdmin +
                ']';
    }
}