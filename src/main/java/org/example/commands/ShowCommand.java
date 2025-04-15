package org.example.commands;
import org.example.managers.CollectionManager;
import org.example.models.StudyGroup;
import org.example.utility.ConsoleReader;
import java.util.LinkedHashSet;

/** Show command: prints all elements. */
public class ShowCommand implements Command {
    private final CollectionManager collectionManager;
    private final ConsoleReader console;

    public ShowCommand(ConsoleReader console, CollectionManager colMgr) {
        this.console = console; this.collectionManager = colMgr; }

    @Override public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) { console.printError("Show command doesn't take arguments."); return true; }
        console.println("--- Collection Elements ---");
        LinkedHashSet<StudyGroup> collection = collectionManager.getCollection();
        if (collection.isEmpty()) {
            console.println("Collection is empty.");
        } else {
            for(StudyGroup group : collection) {
                console.println(group.toString());
            }
        }
        console.println("-------------------------");
        return true;
    }
    @Override public String getName() { return "show"; }
    @Override public String getDescription() { return "show : print all elements of the collection"; }
}