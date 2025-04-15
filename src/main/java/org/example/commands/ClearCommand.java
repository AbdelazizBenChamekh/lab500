package org.example.commands;
import org.example.managers.CollectionManager;
import org.example.utility.ConsoleReader;

/** Clear command: removes all elements. */
public class ClearCommand implements Command {
    private final ConsoleReader console;
    private final CollectionManager collectionManager;

    public ClearCommand(ConsoleReader console, CollectionManager colMgr) {
        this.console = console;
        this.collectionManager = colMgr; }

    @Override
    public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) {
            console.printError("Clear command doesn't take arguments.");
            return true; }
        if (collectionManager.getCollection().isEmpty()) {
            console.println("Collection is already empty.");
        } else {
            collectionManager.clearCollection();
            console.println("Collection cleared.");
        }
        return true;
    }
    @Override
    public String getName() {
        return "clear"; }

    @Override
    public String getDescription() {
        return "clear : clear the collection"; }
}