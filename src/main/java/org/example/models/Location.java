package org.example.models;

import java.util.Objects;

/**
 * Represents a location with coordinates and a name.
 * Contains validation logic.
 */
public class Location {
    private Integer x; //Поле не может быть null
    private double y;
    private String name; //Строка не может быть пустой, Поле может быть null

    /**
     * Constructor for Location.
     * @param x The x coordinate (must not be null).
     * @param y The y coordinate.
     * @param name The location name (can be null, but not empty if provided).
     * @throws IllegalArgumentException If validation fails.
     */
    public Location(Integer x, double y, String name) {
        setX(x);
        this.y = y; // No validation specified for y
        setName(name);
    }

    /**
     * Gets the X coordinate.
     * @return The X coordinate.
     */
    public Integer getX() {
        return x; }

    /**
     * Gets the Y coordinate.
     * @return The Y coordinate.
     */
    public double getY() {
        return y; }

    /**
     * Gets the location name.
     * @return The location name (can be null).
     */
    public String getName() {
        return name; }

    /**
     * Sets the X coordinate with validation.
     * @param x The new X coordinate (must not be null).
     * @throws IllegalArgumentException If x is null.
     */
    public void setX(Integer x) {
        if (x == null) {
            throw new IllegalArgumentException("Location X coordinate cannot be null.");
        }
        this.x = x;
    }

    /**
     * Sets the location name with validation.
     * @param name The new name (can be null, but not empty if provided).
     * @throws IllegalArgumentException If name is empty.
     */
    public void setName(String name) {
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Location name cannot be empty string (but can be null).");
        }
        this.name = name;
    }

    @Override
    public String toString() {
        return "Location{" +
                "x=" + x +
                ", y=" + y +
                ", name='" + (name == null ? "N/A" : name) + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.y, y) == 0 && Objects.equals(x, location.x) && Objects.equals(name, location.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, name);
    }
}