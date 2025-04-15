package org.example.commands;
import org.example.managers.CollectionManager;
import org.example.models.Person;
import org.example.utility.ConsoleReader;
import java.util.List;

/** PrintFieldAscendingGroupAdmin command: prints admins sorted by name. */
public class PrintFieldAscendingGroupAdminCommand implements Command {
    private final ConsoleReader console;
    private final CollectionManager collectionManager;
    public PrintFieldAscendingGroupAdminCommand(ConsoleReader console, CollectionManager colMgr) { this.console = console; this.collectionManager = colMgr; }

    @Override public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) { console.printError(getName() + " command doesn't take arguments."); return true; }
        List<Person> sortedAdmins = collectionManager.getSortedGroupAdmins();
        console.println("--- Group Admins (Ascending by Name) ---");
        if (sortedAdmins.isEmpty()) {
            console.println("No group admins found (collection might be empty).");
        } else {
            for(Person admin : sortedAdmins) {
                console.println(admin.toString());
            }
        }
        console.println("--------------------------------------");
        return true;
    }
    @Override public String getName() { return "print_field_ascending_group_admin"; }
    @Override public String getDescription() { return "print_field_ascending_group_admin : print group admins sorted by name"; }
}