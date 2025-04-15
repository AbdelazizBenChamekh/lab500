package org.example.models;

import java.util.Objects;

/**
 * Represents coordinates (x, y).
 * Contains validation logic.
 */
public class Coordinates {
    private Integer x; //Поле не может быть null
    private Integer y; //Максимальное значение поля: 405, Поле не может быть null

    /**
     * Constructor for Coordinates.
     * @param x The x coordinate (must not be null).
     * @param y The y coordinate (must not be null, must be <= 405).
     * @throws IllegalArgumentException If validation fails.
     */
    public Coordinates(Integer x, Integer y) {
        setX(x);
        setY(y);
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
    public Integer getY() {
        return y; }

    /**
     * Sets the X coordinate with validation.
     * @param x The new X coordinate (must not be null).
     * @throws IllegalArgumentException If x is null.
     */
    public void setX(Integer x) {
        if (x == null) {
            throw new IllegalArgumentException("Coordinates X cannot be null.");
        }
        this.x = x;
    }

    /**
     * Sets the Y coordinate with validation.
     * @param y The new Y coordinate (must not be null, must be <= 405).
     * @throws IllegalArgumentException If y is null or greater than 405.
     */
    public void setY(Integer y) {
        if (y == null) {
            throw new IllegalArgumentException("Coordinates Y cannot be null.");
        }
        if (y > 405) {
            throw new IllegalArgumentException("Coordinates Y cannot be greater than 405. Received: " + y);
        }
        this.y = y;
    }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Objects.equals(x, that.x) && Objects.equals(y, that.y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}