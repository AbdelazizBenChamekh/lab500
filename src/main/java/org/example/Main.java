// File: src/main/java/org/example/Main.java
package org.example;

import org.example.commands.*;
import org.example.managers.CollectionManager;
import org.example.managers.FileManager;
import org.example.utility.ConsoleReader;

import java.util.*;
/**
 * Main application class. Sets up and runs the command loop.
 * Uses ConsoleReader for I/O and manages commands directly via a Map.
 */
public class Main {


    public static final String ENV_VAR_NAME = "STUDY_GROUP_DATA_FILE";

    public static void main(String[] args) {


        try (Scanner mainScanner = new Scanner(System.in)) {


            ConsoleReader console = new ConsoleReader(mainScanner);
            console.println("Starting Study Group Manager...");


            String filePath = System.getenv(ENV_VAR_NAME);
            if (filePath == null || filePath.trim().isEmpty()) {
                console.printError("Warning: Environment variable '" + ENV_VAR_NAME + "' not set.");
                console.println("Running without file load/save capability.");
                filePath = null;
            } else {
                filePath = filePath.trim();
                console.println("Data file path set to: " + filePath);
            }

            // Setup core components
            FileManager fileManager = new FileManager(filePath, console);
            CollectionManager collectionManager = new CollectionManager(fileManager, console);


            Map<String, Command> commandMap = new LinkedHashMap<>();
            List<String> history = new LinkedList<>();
            final int HISTORY_SIZE = 12;


            commandMap.put("help", new HelpCommand(console, commandMap));
            commandMap.put("info", new InfoCommand(console, collectionManager));
            commandMap.put("show", new ShowCommand(console, collectionManager));
            commandMap.put("add", new AddCommand(console, collectionManager));
            commandMap.put("update", new UpdateCommand(collectionManager));
            commandMap.put("remove_by_id", new RemoveByIdCommand(console, collectionManager));
            commandMap.put("clear", new ClearCommand(console, collectionManager));
            commandMap.put("save", new SaveCommand(collectionManager)); // <<< Save only needs CollectionManager
            commandMap.put("exit", new ExitCommand(console));
            commandMap.put("add_if_min", new AddIfMinCommand(collectionManager)); // <<< AddIfMin only needs CollectionManager
            commandMap.put("remove_lower", new RemoveLowerCommand(collectionManager)); // <<< RemoveLower only needs CollectionManager
            commandMap.put("history", new HistoryCommand(console, history)); // History needs history list
            commandMap.put("remove_any_by_form_of_education", new RemoveAnyByFormOfEducationCommand(console, collectionManager));
            commandMap.put("print_ascending", new PrintAscendingCommand(console, collectionManager));
            commandMap.put("print_field_ascending_group_admin", new PrintFieldAscendingGroupAdminCommand(console, collectionManager));
            commandMap.put("execute_script", new ExecuteScriptCommand(console, commandMap, history)); // Execute needs main console, map, history

            console.println(commandMap.size() + " commands registered. Type 'help' for details.");
            console.println("------------------------------------");

            // --- Main Command Loop ---
            boolean keepRunning = true;
            while (keepRunning) {
                console.print("> ");
                try {
                    if (!mainScanner.hasNextLine()) {
                        console.println("\nEnd of input detected. Exiting.");
                        keepRunning = false;
                        continue;
                    }
                    String inputLine = mainScanner.nextLine().trim();
                    if (inputLine.isEmpty()) {
                        continue;
                    }

                    String[] parts = inputLine.split("\\s+", 2);
                    String commandName = parts[0].toLowerCase();
                    String commandArgs = (parts.length > 1) ? parts[1] : null;

                    Command command = commandMap.get(commandName);

                    if (command != null) {
                        // Update history
                        if (history.size() >= HISTORY_SIZE) {
                            history.remove(0);
                        }
                        history.add(commandName);


                        keepRunning = command.execute(commandArgs, console);

                    } else {
                        console.printError("Unknown command: '" + commandName + "'. Type 'help'.");
                    }

                } catch (NoSuchElementException e) {
                    console.printError("\nInput stream closed unexpectedly. Exiting.");
                    keepRunning = false;
                } catch (Exception e) {
                    console.printError("A critical error occurred: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            console.println("Application finished.");

        }
    }
}