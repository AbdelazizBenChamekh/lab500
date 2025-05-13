// File: src/main/java/org/example/client/commands/ClientUpdateCommand.java
package org.example.client.commands;

import org.example.common.models.*;
import org.example.common.network.Request; // Needs Request
import org.example.client.ConsoleReader;
import java.io.Serializable; // Object[] is Serializable if contents are
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

/**
 * Client command to update an element. Reads ID and new object data.
 * Packages ID and StudyGroup data into an Object[] for the Request.
 */
public class ClientUpdateCommand implements ClientCommand { // Implement ClientCommand

    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString == null || argumentString.trim().isEmpty()) { // Trim check added
            console.printError("ID argument is required for update.");
            return null;
        }
        try {
            // 1. Parse the ID
            Integer idToUpdate = Integer.parseInt(argumentString.trim());
            if (idToUpdate <= 0) throw new IllegalArgumentException("ID must be positive.");

            // 2. Read the updated StudyGroup object interactively
            StudyGroup updateData = readStudyGroupInteractively(console, "Enter NEW details for Study Group ID " + idToUpdate + ":");
            if (updateData == null) return null; // Input failed or cancelled

            // 3. Package ID and StudyGroup into an Object array
            Object[] arguments = new Object[2];
            arguments[0] = idToUpdate;    // Store the Integer ID
            arguments[1] = updateData;    // Store the StudyGroup data

            // 4. Create Request with command name and the Object array as the single argument
            return new Request("update", arguments); // <<< FIXED: Pass the array

        } catch (NumberFormatException e) {
            console.printError("Invalid ID format: '" + argumentString + "'. Must be an integer.");
            return null;
        } catch (IllegalArgumentException e) {
            console.printError("Invalid data: " + e.getMessage());
            return null;
        } catch (InputMismatchException e) {
            console.printError("Invalid data entered during object creation: " + e.getMessage());
            return null;
        } catch (NoSuchElementException e) {
            console.printError("Input cancelled during object creation.");
            return null;
        }
    }

    // Helper method (same as before)
    private StudyGroup readStudyGroupInteractively(ConsoleReader console, String headerMessage)
            throws NoSuchElementException, InputMismatchException, IllegalArgumentException {
        console.println(headerMessage);
        String name = console.readNotEmptyString("Enter Group name: ");
        Coordinates coords = console.readCoordinates();
        long studentsCount = console.readLongGreaterThan("Enter Students count (> 0): ", 0);
        Long shouldBeExpelled = console.readNullableLongGreaterThanZero("Enter 'Should Be Expelled' count");
        FormOfEducation form = console.readEnum("Choose Form of Education", FormOfEducation.class, false);
        Semester semester = console.readEnum("Choose Semester", Semester.class, true);
        Person admin = console.readPerson();
        return new StudyGroup(1, name, coords, studentsCount, shouldBeExpelled, form, semester, admin);
    }

    @Override public String getName() { return "update"; }
    @Override public String getDescription() { return "update <id> {element} : update element by id (interactive)"; }
}