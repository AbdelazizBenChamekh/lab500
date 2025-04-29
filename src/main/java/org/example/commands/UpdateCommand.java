package org.example.commands;
import org.example.common.models.*;
import org.example.server.core.CollectionManager;
import org.example.client.ConsoleReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

public class UpdateCommand implements Command {
    private final CollectionManager collectionManager;
    public UpdateCommand(CollectionManager colMgr) {
        this.collectionManager = colMgr; }

    @Override public boolean execute(String args, ConsoleReader console) {
        if (args == null || args.trim().isEmpty()) { console.printError("Missing argument: ID needed. Usage: update <id>"); return true; }
        int id;
        try {
            id = Integer.parseInt(args.trim());
            if (id <= 0) { console.printError("ID must be positive."); return true; }
        } catch (NumberFormatException e) {
            console.printError("Invalid ID format: '" + args.trim() + "'."); return true;
        }

        if (collectionManager.findById(id) == null) {
            console.printError("Element with ID " + id + " not found.");
            return true;
        }

        try {
            console.println("--- Enter/Read NEW Study Group Details for ID: " + id + " ---");
            String name = console.readNotEmptyString("Enter NEW Group name: ");
            Coordinates coords = console.readCoordinates();
            long studentsCount = console.readLongGreaterThan("Enter NEW Students count (> 0): ", 0);
            Long shouldBeExpelled = console.readNullableLongGreaterThanZero("Enter NEW 'Should Be Expelled' count");
            FormOfEducation form = console.readEnum("Choose NEW Form of Education", FormOfEducation.class, false);
            Semester semester = console.readEnum("Choose NEW Semester", Semester.class, true);
            Person admin = console.readPerson();

            StudyGroup updatedData = new StudyGroup(id, name, coords, studentsCount, shouldBeExpelled, form, semester, admin);
            collectionManager.updateElement(id, updatedData);

        } catch (InputMismatchException e) {
            console.printError("Input data format or validation error during update: " + e.getMessage());
            return false; // Signal failure
        } catch (NoSuchElementException e) {
            console.printError("Input ended unexpectedly during 'update' command.");
            return false; // Signal failure
        } catch (IllegalArgumentException e) {
            console.printError("Invalid data for update: " + e.getMessage());
            return false; // Signal failure
        } catch (Exception e) {
            console.printError("An unexpected error occurred during 'update': " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public String getName() {
        return "update"; }
    @Override
    public String getDescription() {
        return "update <id> {element} : update element by id (interactive or from script)"; }
}