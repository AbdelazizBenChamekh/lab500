package org.example.models;

import java.util.Objects;

/**
 * Represents a person (group admin). Implements Comparable for sorting by name.
 * Basic validation included.
 */
public class Person implements Comparable<Person> {
    private String name; // Cannot be null, Cannot be empty
    private Integer weight; // Cannot be null, Must be > 0
    private Color eyeColor; // Can be null
    private Color hairColor; // Cannot be null
    private Country nationality; // Can be null
    private Location location; // Cannot be null

    /**
     * Constructor for Person. Validates input.
     */
    public Person(String name, Integer weight, Color eyeColor, Color hairColor, Country nationality, Location location) {
        // Validation
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Person name cannot be null or empty.");
        }
        this.name = name.trim();


        if (weight == null) {
            throw new IllegalArgumentException("Person weight cannot be null.");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Person weight must be greater than 0. Received: " + weight);
        }
        this.weight = weight;

        this.eyeColor = eyeColor; //0


        if (hairColor == null) {
            throw new IllegalArgumentException("Person hair color cannot be null.");
        }
        this.hairColor = hairColor;

        this.nationality = nationality; // 0


        if (location == null) {
            throw new IllegalArgumentException("Person location cannot be null.");
        }
        this.location = location;
    }


    public String getName() {
        return name; }
    public Integer getWeight() {
        return weight; }
    public Color getEyeColor() {
        return eyeColor; }
    public Color getHairColor() {
        return hairColor; }
    public Country getNationality() {
        return nationality; }
    public Location getLocation() {
        return location; }

    /** Compares persons by name (case-insensitive) for sorting. */
    @Override
    public int compareTo(Person other) {
        return this.name.compareToIgnoreCase(other.name);
    }

    @Override
    public String toString() {
        return "Person[name='" + name + "', weight=" + weight +
                ", eyeColor=" + (eyeColor == null ? "N/A" : eyeColor) +
                ", hairColor=" + hairColor +
                ", nationality=" + (nationality == null ? "N/A" : nationality) +
                ", location=" + location + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(name, person.name) && Objects.equals(weight, person.weight) && eyeColor == person.eyeColor && hairColor == person.hairColor && nationality == person.nationality && Objects.equals(location, person.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight, eyeColor, hairColor, nationality, location);
    }
}