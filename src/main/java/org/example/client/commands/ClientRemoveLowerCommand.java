package org.example.client.commands;

import org.example.common.models.*;
import org.example.common.network.Request;
import org.example.client.ConsoleReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

/**
 * Client command for remove_lower. Reads the threshold element interactively.
 */
public class ClientRemoveLowerCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString != null) {
            console.printError(getName() + " command takes no arguments.");
            return null;
        }
        try {
            console.println("--- Enter details for the threshold element (elements smaller than this will be removed) ---");
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

    @Override public String getName() { return "remove_lower"; }
    @Override public String getDescription() { return "remove_lower {element} : remove elements smaller than the given one (interactive)"; }
}
