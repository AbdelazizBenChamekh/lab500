package org.example.client.commands;

import org.example.common.models.*;
import org.example.common.network.Request;
import org.example.client.ConsoleReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

/**
 * Client command to add a new StudyGroup. Handles interactive object creation.
 */
public class ClientAddCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError("Add command takes no arguments (reads object interactively).");
            return null;
        }

        try {
            console.println("--- Enter details for the new Study Group ---");
            // Use ConsoleReader to get the StudyGroup object interactively
            StudyGroup group = readStudyGroupInteractively(console);
            if (group == null) return null; // User cancelled or error during input

            // Create request with command name and the StudyGroup object
            return new Request("add", group);

        } catch (InputMismatchException | IllegalArgumentException e) {
            console.printError("Invalid data entered during object creation: " + e.getMessage());
            return null;
        } catch (NoSuchElementException e) {
            console.printError("Input cancelled during object creation.");
            return null;
        }
    }

    // Helper stays here or could move to a shared client utility if needed by many commands
    private StudyGroup readStudyGroupInteractively(ConsoleReader console)
            throws NoSuchElementException, InputMismatchException, IllegalArgumentException {
        // This is the same interactive reading logic as before
        String name = console.readNotEmptyString("Enter Group name: ");
        Coordinates coords = console.readCoordinates();
        long studentsCount = console.readLongGreaterThan("Enter Students count (> 0): ", 0);
        Long shouldBeExpelled = console.readNullableLongGreaterThanZero("Enter 'Should Be Expelled' count");
        FormOfEducation form = console.readEnum("Choose Form of Education", FormOfEducation.class, false);
        Semester semester = console.readEnum("Choose Semester", Semester.class, true);
        Person admin = console.readPerson();
        // Dummy ID, server assigns real one
        return new StudyGroup(1, name, coords, studentsCount, shouldBeExpelled, form, semester, admin);
    }

    @Override public String getName() { return "add"; }
    @Override public String getDescription() { return "add {element} : add a new element (interactive input)"; }
}