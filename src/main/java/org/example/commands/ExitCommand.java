package org.example.commands;

import org.example.utility.ConsoleReader;

/** Exit command: terminates the program. */
public class ExitCommand implements Command {
    private final ConsoleReader console;
    public ExitCommand(ConsoleReader console) {
        this.console = console; }

    @Override
    public boolean execute(String args, ConsoleReader console) {
        if (args != null && !args.isEmpty()) { console.printError("Exit command doesn't take arguments.");
            return true; }
        console.println("Exiting program...");
        return false;
    }
    @Override
    public String getName() {
        return ("exit"); }
    @Override
    public String getDescription() {
        return ("exit : terminate program (without saving)"); }
}