package org.example.commands;


import org.example.utility.ConsoleReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Executes commands from a script file. Creates a dedicated ConsoleReader
 * in script mode for non-interactive data input by called commands.
 * Includes refined error handling for script termination and completion messages.
 */
public class ExecuteScriptCommand implements Command {
    private final ConsoleReader mainConsole; // For this command's own messages/errors
    private final Map<String, Command> commandMap; // Map of all available commands
    private static final Set<String> scriptsInExecution = new HashSet<>(); // Prevents recursion
    private final List<String> history; // Command history list

    /**
     * Constructor.
     * @param mainConsole The main console reader (for execute_script's own messages).
     * @param commandMap Map of available commands.
     * @param history List to record command history.
     */
    public ExecuteScriptCommand(ConsoleReader mainConsole, Map<String, Command> commandMap, List<String> history) {
        this.mainConsole = mainConsole;
        this.commandMap = commandMap;
        this.history = history;
    }

    /**
     * Executes the script reading logic.
     * @param args The script file name.
     * @param consoleReader The ConsoleReader from the caller (typically mainConsole, ignored inside).
     * @return true if execution should continue, false if 'exit' was encountered.
     */
    @Override
    public boolean execute(String args, ConsoleReader consoleReader) {

        if (args == null || args.trim().isEmpty()) {
            mainConsole.printError("Missing argument: script file name needed.");
            return true; //Continue
        }
        String fileName = args.trim();
        File scriptFile = new File(fileName);
        String canonicalPath;


        try {
            canonicalPath = scriptFile.getCanonicalPath(); // Resolve path for recursion check
        } catch (IOException e) {
            mainConsole.printError("Error resolving script file path: " + fileName + " - " + e.getMessage());
            return true;
        }
        //Recursion Check
        if (scriptsInExecution.contains(canonicalPath)) {
            mainConsole.printError("Script recursion detected! Stopping execution of '" + fileName + "'.");
            return true; // Continue main loop
        }
        if (!scriptFile.exists() || !scriptFile.isFile()) {
            mainConsole.printError("Script file not found or is a directory: " + fileName);
            return true;
        }
        if (!scriptFile.canRead()) {
            mainConsole.printError("Cannot read script file (check permissions): " + fileName);
            return true;
        }


        boolean continueOverallExecution = true;
        boolean continueScriptProcessing = true;
        boolean scriptCompletedSuccessfully = false;

        scriptsInExecution.add(canonicalPath); // Mark script as executing
        mainConsole.println("--- Executing script: " + fileName + " ---");

        try (Scanner scriptFileReader = new Scanner(scriptFile)) {


            ConsoleReader scriptConsole = new ConsoleReader(scriptFileReader);
            scriptConsole.setScriptMode(true);


            while (continueScriptProcessing && scriptFileReader.hasNextLine()) {
                String line;
                try {

                    line = scriptFileReader.nextLine().trim();
                } catch (NoSuchElementException e) {

                    mainConsole.printError("Script ended unexpectedly while reading command line.");
                    continueScriptProcessing = false;
                    break;
                }


                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                mainConsole.println("Script> " + line);


                String[] parts = line.split("\\s+", 2);
                String commandName = parts[0].toLowerCase();
                String commandArgs = (parts.length > 1) ? parts[1] : null;

                Command commandToExecute = commandMap.get(commandName);

                if (commandToExecute != null) {
                    // Update history
                    if (history.size() >= 12) history.remove(0);
                    history.add(commandName);

                    // Execute the command, passing the scriptConsole
                    try {
                        boolean commandResult = commandToExecute.execute(commandArgs, scriptConsole);


                        if (!commandResult) {
                            if (commandToExecute instanceof ExitCommand) {
                                continueOverallExecution = false;
                                mainConsole.println("(Exit command executed within script)");
                            } else {
                                mainConsole.println("(Command '" + commandName + "' indicated script failure)");
                            }
                            continueScriptProcessing = false; // Stop processing *this* script
                        }

                    } catch (InputMismatchException | IllegalArgumentException e) {
                        mainConsole.printError("Invalid data format or value in script for command '" + commandName + "': " + e.getMessage());
                        continueScriptProcessing = false; // Stop script processing on bad data.
                    } catch (Exception e) {
                        // Catch any other unexpected errors from the command itself
                        mainConsole.printError("Error during execution of script command '" + commandName + "': " + e.getMessage());
                        e.printStackTrace();
                        continueScriptProcessing = false;
                    }
                } else {
                    mainConsole.printError("Unknown command in script: '" + commandName + "'. Skipping.");
                }
            }
            if (continueScriptProcessing && !scriptFileReader.hasNextLine()) {
                scriptCompletedSuccessfully = true;
            }

        } catch (FileNotFoundException | SecurityException e) {
            // Errors opening or accessing the script file itself
            mainConsole.printError("Error accessing script file '" + fileName + "': " + e.getMessage());

        } catch (Exception e) {
            // Catch any other unexpected error during the overall script processing setup/teardown
            mainConsole.printError("An unexpected error occurred involving script '" + fileName + "': " + e.getMessage());
            e.printStackTrace();

        } finally {

            scriptsInExecution.remove(canonicalPath);

            if (scriptCompletedSuccessfully) {
                mainConsole.println("--- Finished script: " + fileName + " (End of file reached successfully) ---");
            } else {

                mainConsole.println("--- Script execution halted: " + fileName + " ---");
            }
        }


        return continueOverallExecution;
    }

    @Override public String getName() { return "execute_script"; }
    @Override public String getDescription() { return "execute_script <file_name> : execute script from file"; }
}