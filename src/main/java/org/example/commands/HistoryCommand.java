package org.example.commands;
import org.example.client.ConsoleReader;
import java.util.List; // Need List

/** History command: displays last N commands. */
public class HistoryCommand implements Command {
    private final ConsoleReader console;
    private final List<String> history; // Needs access to the history list

    public HistoryCommand(ConsoleReader console, List<String> history) {
        this.console = console;
        this.history = history;
    }

    @Override public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) { console.printError("History command doesn't take arguments."); return true; }
        console.println("--- Command History (last " + history.size() + ") ---");
        if (history.isEmpty()) {
            console.println("No commands executed yet.");
        } else {
            for (String cmdName : history) {
                console.println(cmdName);
            }
        }
        console.println("-----------------------------");
        return true;
    }
    @Override
    public String getName() {
        return ("history"); }
    @Override
    public String getDescription() {
        return ("history : print the last 12 executed commands"); }
}