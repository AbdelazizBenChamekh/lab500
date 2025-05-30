package org.example.common.utility;

/**
 * Class for coloring console output
 */
public enum ConsoleColors {
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    RESET("\u001B[0m"),
    WHITE("\u001B[37m");

    private final String title;

    ConsoleColors(String title) {
        this.title = title;
    }

    /**
     * Basic method for coloring text
     * @param s string to be colored
     * @param color color value
     * @return colored string to output to console
     */
    public static String toColor(String s, ConsoleColors color){
        return color + s + ConsoleColors.RESET;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}