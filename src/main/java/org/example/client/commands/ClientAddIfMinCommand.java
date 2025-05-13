package org.example.client.commands;

import org.example.common.models.*;
import org.example.common.network.Request;
import org.example.client.ConsoleReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

/**
 * Client command for add_if_min. Reads the candidate element interactively.
 */
public class ClientAddIfMinCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError(getName() + " command takes no arguments.");
            return null;
        }
        try {
            console.println("--- Enter details for the element to potentially add (if min) ---");
            StudyGroup group = readStudyGroupInteractively(console); // Reuse helper
            if (group == null) return null;
            return new Request(getName(), group); // Send command + object
        } catch (InputMismatchException | IllegalArgumentException e) {
            console.printError("Invalid data entered: " + e.getMessage());
            return null;
        } catch (NoSuchElementException e) {
            console.printError("Input cancelled during object creation.");
            return null;
        }
    }

    // Copied helper (better in shared utility)
    private StudyGroup readStudyGroupInteractively(ConsoleReader console)
            throws NoSuchElementException, InputMismatchException, IllegalArgumentException {
        String name = console.readNotEmptyString("Enter Group name: ");
        Coordinates coords = console.readCoordinates();
        long studentsCount = console.readLongGreaterThan("Enter Students count (> 0): ", 0);
        Long shouldBeExpelled = console.readNullableLongGreaterThanZero("Enter 'Should Be Expelled' count");
        FormOfEducation form = console.readEnum("Choose Form of Education", FormOfEducation.class, false);
        Semester semester = console.readEnum("Choose Semester", Semester.class, true);
        Person admin = console.readPerson();
        return new StudyGroup(1, name, coords, studentsCount, shouldBeExpelled, form, semester, admin);
    }

    @Override public String getName() { return "add_if_min"; }
    @Override public String getDescription() { return "add_if_min {element} : add element if it's smaller than server's minimum (interactive)"; }
}