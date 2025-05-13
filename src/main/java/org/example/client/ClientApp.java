package org.example.client;

import org.example.client.commands.ClientCommand;
import org.example.client.commands.ClientExecuteScriptCommand;
import org.example.client.commands.ClientHelpCommand;
import org.example.client.commands.ClientHistoryCommand;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.common.models.StudyGroup;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientApp {
    private final ConsoleReader consoleReader;
    private final ClientCommandManager commandManager;
    private ClientNetworkManager networkManager;

    public ClientApp(String host, int port) {
        this.consoleReader = new ConsoleReader(new Scanner(System.in));
        this.commandManager = new ClientCommandManager(this);
        try {
            this.networkManager = new ClientNetworkManager(host, port, consoleReader);
        } catch (IOException e) {
            consoleReader.printError("FATAL: Could not initialize network manager: " + e.getMessage());
            this.networkManager = null;
        }
    }

    public void run() {
        if (networkManager == null) {
            consoleReader.printError("Exiting due to network initialization failure.");
            return;
        }

        boolean keepRunning = true;
        while (keepRunning) {
            try {
                consoleReader.print("> ");
                String inputLine = consoleReader.readNotEmptyString("");

                String[] parts = inputLine.trim().split("\\s+", 2);
                String commandName = parts[0].toLowerCase();
                String argumentString = (parts.length > 1) ? parts[1] : null;

                if (commandName.equals("exit")) {
                    keepRunning = false;
                    consoleReader.println("[Client] Exiting.");
                    continue;
                }

                ClientCommand command = commandManager.getCommand(commandName);

                if (command != null) {
                    Request requestToSend = command.prepareRequest(argumentString, consoleReader);

                    if (!(command instanceof ClientExecuteScriptCommand)) {
                        if (requestToSend != null ||
                                command instanceof ClientHelpCommand ||
                                command instanceof ClientHistoryCommand) {
                            commandManager.addToHistory(commandName);
                        }
                    }

                    if (requestToSend != null) {
                        sendRequestAndProcessResponse(requestToSend);
                    }

                } else {
                    consoleReader.printError("Unknown command: '" + commandName + "'. Type 'help'.");
                }

            } catch (NoSuchElementException e) {
                consoleReader.println("\n[Client] Input stream closed. Exiting.");
                keepRunning = false;
            } catch (Exception e) {
                consoleReader.printError("An unexpected client error occurred in main loop: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            if (networkManager != null) {
                networkManager.close();
            }
        } catch (Exception e) {
            consoleReader.printError("Error closing network manager: " + e.getMessage());
        }
    }

    public void sendRequestAndProcessResponse(Request request) {
        if (request == null) {
            consoleReader.printError("[Internal Error] Attempted to send a null request.");
            return;
        }
        if (networkManager.sendRequest(request)) {
            Response response = networkManager.receiveResponse();
            if (response != null) {
                displayResponse(response);
            }
        } else {
            consoleReader.printError("Request not sent. Skipping response wait.");
        }
    }

    private void displayResponse(Response response) {
        consoleReader.println("\n<<< Server Response <<<");
        consoleReader.println("Status: " + response.getStatusCode());

        if (response.getResponseBody() != null && !response.getResponseBody().isEmpty()) {
            String[] lines = response.getResponseBody().split("\n");
            for(String line : lines){
                consoleReader.println(line);
            }
        }

        List<StudyGroup> groups = response.getCollectionData();
        if (groups != null) {
            if (groups.isEmpty()) {
                consoleReader.println("(Received empty collection)");
            } else {
                consoleReader.println("--- Received Collection Data (" + groups.size() + " items, sorted by size by server) ---");
                for (StudyGroup group : groups) {
                    consoleReader.println("  " + group.toString());
                }
                consoleReader.println("----------------------------------");
            }
        }

        if ((response.getResponseBody() != null && !response.getResponseBody().isEmpty()) || groups != null) {
            consoleReader.println(">>> End Server Response >>>");
        } else if (response.getStatusCode() == StatusCode.OK) {
            consoleReader.println("(Operation completed successfully by server)");
            consoleReader.println(">>> End Server Response >>>");
        } else {
            consoleReader.println(">>> End Server Response >>>");
        }
        consoleReader.println("");
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java org.example.client.ClientApp <server_host> <server_port>");
            System.exit(1);
        }
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port <= 0 || port > 65535) throw new NumberFormatException("Port must be 1-65535");
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[1] + ". " + e.getMessage());
            System.exit(1);
            return;
        }

        ClientApp client = new ClientApp(host, port);
        client.run();
    }
}