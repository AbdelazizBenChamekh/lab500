package org.example.commands;
import org.example.server.core.CollectionManager;
import org.example.client.ConsoleReader;

/** RemoveById command: removes element matching the given ID. */
public class RemoveByIdCommand implements Command {
    private final ConsoleReader console;
    private final CollectionManager collectionManager;
    public RemoveByIdCommand(ConsoleReader console, CollectionManager colMgr) { this.console = console; this.collectionManager = colMgr; }

    @Override public boolean execute(String args, ConsoleReader console) {
        if (args == null || args.trim().isEmpty()) { console.printError("Missing argument: ID needed. Usage: remove_by_id <id>"); return true; }
        int id;
        try {
            id = Integer.parseInt(args.trim());
            if (id <= 0) { console.printError("ID must be positive."); return true; }
        } catch (NumberFormatException e) {
            console.printError("Invalid ID format: '" + args.trim() + "'."); return true;
        }

        if (collectionManager.removeById(id)) {
            console.println("Element with ID " + id + " removed.");
        } else {
            console.printError("Element with ID " + id + " not found.");
        }
        return true;
    }
    @Override
    public String getName() {
        return "remove_by_id"; }
    @Override
    public String getDescription() {
        return "remove_by_id <id> : remove element by its id"; }
}
