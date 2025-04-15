
package org.example.commands;


import org.example.managers.CollectionManager;
import org.example.utility.ConsoleReader;

/**
 * Save command: triggers saving the collection to file via CollectionManager.
 * Uses the ConsoleReader passed to execute for any messages.
 */
public class SaveCommand implements Command {

    private final CollectionManager collectionManager;

    /**
     * Constructor.
     * @param colMgr The CollectionManager to use for saving.
     */
    public SaveCommand(CollectionManager colMgr) {
        this.collectionManager = colMgr;
    }

    /**
     * Executes the save action.
     * @param args Arguments (ignored by this command).
     * @param console The ConsoleReader passed by the caller (Main or ExecuteScript).
     * @return true always.
     */
    @Override
    public boolean execute(String args, ConsoleReader console) {

        if (args != null && !args.isEmpty()) {
            console.printError("Save command doesn't take arguments.");
            return true;
        }


        try {
            collectionManager.saveCollection();
        } catch (Exception e) {
            console.printError("An unexpected error occurred during save: " + e.getMessage());
        }
        return true; // Always continue the application after a save attempt
    }

    @Override
    public String getName() { return "save"; }
    @Override public String getDescription() { return "save : save collection to file"; }
}