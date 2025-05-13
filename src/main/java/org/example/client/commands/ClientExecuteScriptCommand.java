package org.example.client.commands;

import org.example.client.ClientApp;
import org.example.common.network.Request; // Doesn't create requests itself directly
import org.example.client.ConsoleReader; // Needs console for messages
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*; // Scanner, Map, List, Set, HashSet etc.

/**
 * Client-side execute_script command. Reads script file and processes commands locally.
 * For server commands, it delegates Request creation to other client Command objects.
 */
public class ClientExecuteScriptCommand implements ClientCommand {
    private final Map<String, ClientCommand> commandMap; // Needs access to other commands
    private final List<String> history;          // Needs access to history
    private final Set<String> scriptStack;       // Needs access to script stack for recursion check
    private final ClientApp clientApp;           // <<< Needs reference to ClientApp to send requests/process responses

    public ClientExecuteScriptCommand(Map<String, ClientCommand> commandMap, List<String> history, Set<String> scriptStack, ClientApp clientApp) {
        this.commandMap = commandMap;
        this.history = history;
        this.scriptStack = scriptStack;
        this.clientApp = clientApp;
    }

    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString == null) {
            console.printError("Missing argument: script file name needed.");
            return null; // Cannot prepare a request without the filename
        }

        File scriptFile = new File(argumentString.trim());
        String canonicalPath;
        try {
            canonicalPath = scriptFile.getCanonicalPath();
        } catch (IOException e) {
            console.printError("Error resolving script file path: " + argumentString + " - " + e.getMessage());
            return null;
        }

        // --- Recursion Check ---
        if (scriptStack.contains(canonicalPath)) {
            console.printError("Script recursion detected! Aborting script '" + argumentString + "'.");
            return null; // Indicate failure, no request needed
        }
        // --- File Checks ---
        if (!scriptFile.exists() || !scriptFile.isFile() || !scriptFile.canRead()) {
            console.printError("Script file not found, is not a file, or cannot be read: " + argumentString);
            return null;
        }

        console.println("--- Executing script: " + argumentString + " ---");
        scriptStack.add(canonicalPath); // Add BEFORE execution

        try (Scanner scriptScanner = new Scanner(scriptFile)) {
            while (scriptScanner.hasNextLine()) {
                String line = scriptScanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                console.println("Script> " + line); // Echo line from script

                String[] parts = line.split("\\s+", 2);
                String commandName = parts[0].toLowerCase();
                String scriptCommandArgs = (parts.length > 1) ? parts[1] : null;

                // Find the corresponding CLIENT command object
                ClientCommand commandToProcess = commandMap.get(commandName);

                if (commandToProcess != null) {
                    // Add script command to history
                    if (history.size() >= 12) history.remove(0);
                    history.add(commandName);

                    if (commandToProcess instanceof ClientExecuteScriptCommand) {
                        // Handle nested execution directly without creating a Request
                        ((ClientExecuteScriptCommand) commandToProcess).prepareRequest(scriptCommandArgs, console);
                        // Note: This recursive call handles its own scriptStack add/remove
                    } else if (commandToProcess instanceof ClientHistoryCommand) {
                        // Handle history locally
                        ((ClientHistoryCommand) commandToProcess).executeLocally(console);
                    } else if (commandName.equals("exit")) {
                        console.printError("'exit' command encountered in script. Halting script execution.");
                        break; // Stop processing this script file
                    }
                    // ... handle other purely client-side commands if any ...
                    else {
                        // For commands that need server interaction:
                        // 1. Prepare the request using the client command object
                        Request requestToSend = commandToProcess.prepareRequest(scriptCommandArgs, console);

                        // 2. If request prepared successfully, send it via ClientApp
                        if (requestToSend != null) {
                            // Delegate sending and response processing back to ClientApp
                            clientApp.sendRequestAndProcessResponse(requestToSend);
                        } else {
                            // prepareRequest failed (e.g., bad args, user cancelled input)
                            console.printError("Failed to prepare request for script command: " + line + ". Halting script.");
                            break; // Stop processing script on error
                        }
                    }
                } else {
                    console.printError("Unknown command in script: '" + commandName + "'. Skipping.");
                }
            } // end while loop

        } catch (FileNotFoundException e) {
            console.printError("Script file not found during execution: " + argumentString);
        } catch (Exception e) {
            console.printError("Unexpected error during script execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scriptStack.remove(canonicalPath); // Remove from stack when done/error
            console.println("--- Finished script: " + argumentString + " ---");
        }

        // ExecuteScript itself doesn't return a Request to send, it handles sending internally
        return null;
    }

    @Override public String getName() { return "execute_script"; }
    @Override public String getDescription() { return "execute_script <file> : execute commands from a script file"; }
}