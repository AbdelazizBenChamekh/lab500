// File: src/main/java/org/example/commands/AddCommand.java
package org.example.commands;

import org.example.managers.CollectionManager;
import org.example.models.*;
import org.example.utility.ConsoleReader;

import java.util.InputMismatchException;
import java.util.NoSuchElementException;

/**
 * Add command: Adds element. Uses the provided ConsoleReader.
 * Returns false if input ends unexpectedly (e.g., script ends).
 * Throws exceptions for validation errors caught by models.
 */
public class AddCommand implements Command {
    private final CollectionManager collectionManager;

    public AddCommand(ConsoleReader console, CollectionManager colMgr) {
        this.collectionManager = colMgr;
    }

    /**
     * Executes the add logic using the provided ConsoleReader.
     * @param args Ignored by this command.
     * @param console The ConsoleReader to use (could be main or script-based).
     * @return true if the command logically attempted execution,
     *         false if input ended unexpectedly (signals script failure).
     * @throws InputMismatchException if data validation fails within ConsoleReader helpers.
     * @throws IllegalArgumentException if validation fails within Model constructors.
     */
    @Override
    public boolean execute(String args, ConsoleReader console)
            throws InputMismatchException, IllegalArgumentException {

        if (args != null && !args.isEmpty()) {
            console.printError(getName() + " command doesn't take arguments.");
            return true;
        }

        try {
            console.println("--- Enter/Read Study Group Details ---");
            String name = console.readNotEmptyString("Enter Group name: ");
            Coordinates coords = console.readCoordinates();
            long studentsCount = console.readLongGreaterThan("Enter Students count (> 0): ", 0);
            Long shouldBeExpelled = console.readNullableLongGreaterThanZero("Enter 'Should Be Expelled' count");
            FormOfEducation form = console.readEnum("Choose Form of Education", FormOfEducation.class, false);
            Semester semester = console.readEnum("Choose Semester", Semester.class, true);
            Person admin = console.readPerson();



            StudyGroup tempGroup = new StudyGroup(1, name, coords, studentsCount, shouldBeExpelled, form, semester, admin);

            collectionManager.addElement(tempGroup);
            return true;

        } catch (NoSuchElementException e) {
            console.printError("Input ended unexpectedly during 'add' command.");
            return false;
        }
    }

    @Override
    public String getName() {
        return "add"; }
    @Override
    public String getDescription() {
        return "add {element} : add a new element (interactive or from script)"; }
}