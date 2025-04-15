package org.example.commands;
import org.example.managers.CollectionManager;
import org.example.utility.ConsoleReader;

/** Info command: shows collection metadata. */
public class InfoCommand implements Command {
    private final CollectionManager collectionManager;


    public InfoCommand(ConsoleReader console, CollectionManager colMgr) {
        this.collectionManager = colMgr;
    }

    @Override
    public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) {
            console.printError("Info command doesn't take arguments.");
            return true;
        }
        console.println("--- Collection Information ---");
        console.println(collectionManager.getInfo());
        console.println("----------------------------");
        return true;
    }

    @Override
    public String getName() {
        return "info"; }
    @Override public String getDescription() {
        return "info : print information about the collection"; }
}