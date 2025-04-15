package org.example.commands;
import org.example.managers.CollectionManager;
import org.example.models.FormOfEducation;
import org.example.utility.ConsoleReader;

/** RemoveAnyByFormOfEducation command: removes one element matching the form. */
public class RemoveAnyByFormOfEducationCommand implements Command {
    private final ConsoleReader console;
    private final CollectionManager collectionManager;
    public RemoveAnyByFormOfEducationCommand(ConsoleReader console, CollectionManager colMgr) { this.console = console; this.collectionManager = colMgr; }

    @Override
    public boolean execute(String args, ConsoleReader console) {
        if (args == null || args.trim().isEmpty()) {
            console.printError("Missing argument: FormOfEducation needed.");
            // Show possible values (readEnum does this, but good here too)
            console.print("Possible values: ");
            for(FormOfEducation f : FormOfEducation.values()) { console.print(f.name() + " "); }
            console.println("");
            return true;
        }

        try {
            FormOfEducation formToFind = FormOfEducation.valueOf(args.trim().toUpperCase());
            if (collectionManager.removeAnyByFormOfEducation(formToFind)) {
                console.println("Removed one element with form " + formToFind);
            } else {
                console.println("No element found with form " + formToFind);
            }
        } catch (IllegalArgumentException e) {
            console.printError("Invalid FormOfEducation value: '" + args.trim() + "'.");
            console.print("Use one of: ");
            for(FormOfEducation f : FormOfEducation.values()) { console.print(f.name() + " "); }
            console.println("");
        }
        return true;
    }
    @Override
    public String getName() {
        return "remove_any_by_form_of_education"; }
    @Override
    public String getDescription() {
        return "remove_any_by_form_of_education <form> : remove one element by form of education"; }
}