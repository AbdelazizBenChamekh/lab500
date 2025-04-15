package org.example.commands;
import org.example.managers.CollectionManager;
import org.example.models.StudyGroup;
import org.example.utility.ConsoleReader;
import java.util.List;

/** PrintAscending command: prints elements sorted by ID. */
public class PrintAscendingCommand implements Command {
    private final ConsoleReader console;
    private final CollectionManager collectionManager;
    public PrintAscendingCommand(ConsoleReader console, CollectionManager colMgr) { this.console = console; this.collectionManager = colMgr; }

    @Override public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) { console.printError(getName() + " command doesn't take arguments."); return true; }
        List<StudyGroup> sortedList = collectionManager.getSortedElements();
        console.println("--- Collection Elements (Ascending by ID) ---");
        if (sortedList.isEmpty()) {
            console.println("Collection is empty.");
        } else {
            for(StudyGroup group : sortedList) {
                console.println(group.toString());
            }
        }
        console.println("-------------------------------------------");
        return true;
    }
    @Override
    public String getName() {
        return "print_ascending"; }
    @Override
    public String getDescription() {
        return "print_ascending : print elements sorted by id"; }
}