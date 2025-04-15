package org.example.commands;
import org.example.managers.CollectionManager;
import org.example.models.*;
import org.example.utility.ConsoleReader;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

public class RemoveLowerCommand implements Command {
    private final CollectionManager collectionManager;
    public RemoveLowerCommand(CollectionManager colMgr) { this.collectionManager = colMgr; }

    @Override public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) { console.printError(getName() + " command doesn't take arguments."); return true; }
        try {
            console.println("--- Enter/Read Threshold Element Details for RemoveLower ---");
            String name = console.readNotEmptyString("Enter Group name: ");
            Coordinates coords = console.readCoordinates();
            long studentsCount = console.readLongGreaterThan("Enter Students count (> 0): ", 0);
            Long shouldBeExpelled = console.readNullableLongGreaterThanZero("Enter 'Should Be Expelled' count");
            FormOfEducation form = console.readEnum("Choose Form of Education", FormOfEducation.class, false);
            Semester semester = console.readEnum("Choose Semester", Semester.class, true);
            Person admin = console.readPerson();

            StudyGroup thresholdData = new StudyGroup(1, name, coords, studentsCount, shouldBeExpelled, form, semester, admin);
            collectionManager.removeLower(thresholdData);

        } catch (InputMismatchException e) {
            console.printError("Input data format or validation error: " + e.getMessage());
            return false;
        } catch (NoSuchElementException e) {
            console.printError("Input ended unexpectedly during 'remove_lower' command.");
            return false;
        } catch (IllegalArgumentException e) {
            console.printError("Invalid data for threshold element: " + e.getMessage());
            return false;
        } catch (Exception e) {
            console.printError("An unexpected error occurred during 'remove_lower': " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public String getName() {
        return "remove_lower"; }
    @Override
    public String getDescription() {
        return "remove_lower {element} : remove elements smaller than given (interactive or script)"; }
}