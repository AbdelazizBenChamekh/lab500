package org.example.client;


import org.example.common.models.*;
import org.example.common.network.Request;
import org.example.common.network.Response;


import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel; // For non-blocking UDP
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector; // For non-blocking I/O multiplexing
import java.util.*;

/**
 * The main application class for the client.
 * Connects to the server via UDP, sends command requests (using Request class),
 * and displays responses (using Response class) via non-blocking NIO channels.
 */
public class ClientApp {
    private static final int SERVER_RESPONSE_TIMEOUT_MS = 5000; // 5 seconds
    private static final int BUFFER_SIZE = 8192; // Match server buffer size

    private final String serverHost;
    private final int serverPort;
    private DatagramChannel channel;
    private final ConsoleReader consoleReader; // For user interaction
    private Selector selector; // For non-blocking I/O
    private final List<String> history = new LinkedList<>(); // Client-side history
    private static final int HISTORY_SIZE = 12;
    private final Set<String> scriptStack = new HashSet<>(); // For execute_script recursion check

    public ClientApp(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        // Client uses ConsoleReader linked to System.in for interaction
        this.consoleReader = new ConsoleReader(new Scanner(System.in));
    }

    /**
     * Initializes the non-blocking network channel and selector.
     * @return true if successful, false otherwise.
     */
    private boolean initializeNetwork() {
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false); // Set to non-blocking
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ); // Interested in reading responses
            System.out.println("[Client] Network channel initialized for server " + serverHost + ":" + serverPort);
            return true;
        } catch (IOException e) {
            consoleReader.printError("Client network initialization failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Starts the main client loop for reading commands and interacting with the server.
     */
    public void run() {
        if (!initializeNetwork()) {
            return; // Exit if network failed
        }

        boolean keepRunning = true;
        while (keepRunning) {
            try {
                consoleReader.print("> ");
                String inputLine = consoleReader.readNotEmptyString(""); // Read command line

                // --- Client-Side Command Handling ---
                String[] parts = inputLine.trim().split("\\s+", 2);
                String commandName = parts[0].toLowerCase();
                String argumentString = (parts.length > 1) ? parts[1] : null;

                if (commandName.equals("exit")) {
                    keepRunning = false;
                    System.out.println("[Client] Exiting.");
                    continue;
                }

                if (commandName.equals("history")) {
                    handleHistory();
                    continue; // Don't send history to server
                }

                if (commandName.equals("execute_script")) {
                    if (argumentString == null) {
                        consoleReader.printError("Missing argument: script file name needed.");
                        continue;
                    }
                    executeScript(argumentString); // Handle script execution locally
                    continue; // Don't send execute_script command itself to server
                }

                if (commandName.equals("save")) {
                    consoleReader.printError("'save' command is not available on the client.");
                    continue; // Don't send save to server
                }
                // --- End Client-Side Handling ---


                // --- Prepare Request for Server ---
                Request request = parseAndPrepareRequest(commandName, argumentString);
                if (request == null) {
                    // Error message already printed by parseAndPrepareRequest
                    continue; // Skip sending if request creation failed
                }

                // Add command to client history (only if it's sent to server)
                addToHistory(commandName);


                // Send request and process response
                sendRequest(request);
                processResponse();

            } catch (NoSuchElementException e) {

                System.out.println("\n[Client] Input stream closed. Exiting.");
                keepRunning = false;
            } catch (Exception e) { // Catch broader unexpected errors
                consoleReader.printError("An unexpected client error occurred: " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
                // keepRunning = false; // Optional: exit on major errors
            }
        }

        closeResources(); // Clean up network resources on exit
    }

    /**
     * Parses user input, potentially reads object data interactively,
     * and creates the Request object based on the target command.
     * @param commandName The command name entered by the user.
     * @param argumentString The string part of the arguments entered by the user (can be null).
     * @return The prepared Request object, or null if input/parsing failed.
     */
    private Request parseAndPrepareRequest(String commandName, String argumentString) {
        Object commandArgument = null; // The single argument object for the Request

        try {
            switch (commandName) {
                // Commands requiring a StudyGroup object argument:
                case "add":
                case "add_if_min":
                case "remove_lower":
                    commandArgument = readStudyGroupInteractively("Enter details for the Study Group:");
                    if (commandArgument == null) return null; // Input cancelled or failed
                    break;

                case "update":
                    if (argumentString == null) {
                        consoleReader.printError("ID argument is required for update.");
                        return null;
                    }
                    Integer idToUpdate = Integer.parseInt(argumentString.trim()); // Can throw NumberFormatException
                    if (idToUpdate <=0) throw new IllegalArgumentException("ID must be positive.");
                    StudyGroup updateData = readStudyGroupInteractively("Enter NEW details for Study Group ID " + idToUpdate + ":");
                    if (updateData == null) return null; // Input cancelled or failed
                    // Package ID and StudyGroup into an Object array
                    commandArgument = new Object[]{idToUpdate, updateData};
                    break;

                // Commands requiring a specific type parsed from stringArg:
                case "remove_by_id":
                    if (argumentString == null) {
                        consoleReader.printError("ID argument is required for remove_by_id.");
                        return null;
                    }
                    Integer idToRemove = Integer.parseInt(argumentString.trim()); // Can throw NumberFormatException
                    if (idToRemove <=0) throw new IllegalArgumentException("ID must be positive.");
                    commandArgument = idToRemove;
                    break;

                case "remove_any_by_form_of_education":
                    if (argumentString == null) {
                        consoleReader.printError("FormOfEducation argument required.");
                        // You could print valid forms here using FormOfEducation.values()
                        return null;
                    }
                    // Convert string to enum
                    FormOfEducation form = FormOfEducation.valueOf(argumentString.trim().toUpperCase());
                    commandArgument = form;
                    break;

                // Commands taking no specific argument (argument is null):
                case "help":
                case "info":
                case "show":
                case "clear":
                case "print_ascending":
                case "print_field_ascending_group_admin":
                    if (argumentString != null) {
                        consoleReader.printError("Command '" + commandName + "' does not take arguments.");
                        // Decide if this should prevent sending. Let's allow sending, server might handle it.
                    }
                    commandArgument = null;
                    break; // Argument remains null

                // Commands handled entirely client-side (shouldn't reach here)
                // case "exit":
                // case "history":
                // case "execute_script":
                // case "save":

                default:
                    // If command wasn't handled above, assume it's unknown or server-side only with no specific client args needed.
                    // Or, we could return null here to prevent sending unknown commands. Let's allow sending.
                    consoleReader.println("[Client Note] Sending command '" + commandName + "' with argument: " + (argumentString != null ? "'" + argumentString + "'" : "null"));
                    commandArgument = argumentString; // Send the raw string arg if present, otherwise null
                    // Server will ultimately decide if the command and argument are valid.
                    break;
            }

            // Construct the request
            return new Request(commandName, commandArgument);

        } catch (InputMismatchException | IllegalArgumentException e) {
            // Catch data format, validation errors from ConsoleReader or parsing
            consoleReader.printError("Invalid data entered: " + e.getMessage());
            return null; // Failed to create request
        } catch (NoSuchElementException e) {
            consoleReader.printError("Input cancelled during object creation.");
            return null; // Failed to create request
        }
    }

    /**
     * Helper method to interactively read StudyGroup details using ConsoleReader.
     * @param headerMessage Message to print before starting input.
     * @return The constructed StudyGroup, or null if input cancelled/failed.
     */
    private StudyGroup readStudyGroupInteractively(String headerMessage)
            throws NoSuchElementException, InputMismatchException, IllegalArgumentException {
        // This method now throws exceptions up to the caller (parseAndPrepareRequest)
        consoleReader.println(headerMessage);
        String name = consoleReader.readNotEmptyString("Enter Group name: ");
        Coordinates coords = consoleReader.readCoordinates();
        long studentsCount = consoleReader.readLongGreaterThan("Enter Students count (> 0): ", 0);
        Long shouldBeExpelled = consoleReader.readNullableLongGreaterThanZero("Enter 'Should Be Expelled' count");
        FormOfEducation form = consoleReader.readEnum("Choose Form of Education", FormOfEducation.class, false);
        Semester semester = consoleReader.readEnum("Choose Semester", Semester.class, true);
        Person admin = consoleReader.readPerson();
        // Return a temporary object. Server handles final ID/Date.
        // Use dummy ID 1 for constructor validation.
        return new StudyGroup(1, name, coords, studentsCount, shouldBeExpelled, form, semester, admin);
    }

    /** Adds command name to history buffer. */
    private void addToHistory(String commandName) {
        if (commandName == null || commandName.trim().isEmpty()) return;
        if (history.size() >= HISTORY_SIZE) {
            history.remove(0); // Remove oldest
        }
        history.add(commandName.toLowerCase());
    }

    /** Handles the client-side history command. */
    private void handleHistory() {
        consoleReader.println("--- Client Command History (last " + history.size() + ") ---");
        if (history.isEmpty()) {
            consoleReader.println("(No commands executed yet in this session)");
        } else {
            int count = 1;
            for (String cmd : history) {
                consoleReader.println(String.format("%2d. %s", count++, cmd));
            }
        }
        consoleReader.println("------------------------------------");
    }


    /** Handles the client-side execute_script command. */
    private void executeScript(String fileName) {
        File scriptFile = new File(fileName);
        String canonicalPath;
        try {
            canonicalPath = scriptFile.getCanonicalPath();
        } catch (IOException e) {
            consoleReader.printError("Error resolving script file path: " + fileName + " - " + e.getMessage());
            return;
        }

        // Recursion Check
        if (scriptStack.contains(canonicalPath)) {
            consoleReader.printError("Script recursion detected! Aborting script '" + fileName + "'.");
            return;
        }
        if (!scriptFile.exists() || !scriptFile.isFile() || !scriptFile.canRead()) {
            consoleReader.printError("Script file not found, is not a file, or cannot be read: " + fileName);
            return;
        }

        consoleReader.println("--- Executing script: " + fileName + " ---");
        scriptStack.add(canonicalPath); // Add to stack BEFORE execution

        try (Scanner scriptScanner = new Scanner(scriptFile)) {
            while (scriptScanner.hasNextLine()) {
                String line = scriptScanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                consoleReader.println("Script> " + line); // Echo script line

                // --- Process line as if typed by user ---
                String[] parts = line.split("\\s+", 2);
                String commandName = parts[0].toLowerCase();
                String argumentString = (parts.length > 1) ? parts[1] : null;

                // Handle nested script execution first
                if (commandName.equals("execute_script")) {
                    if (argumentString == null) {
                        consoleReader.printError("Missing script file name in nested execute_script.");
                    } else {
                        executeScript(argumentString);
                    }
                    continue; // Move to next script line
                }

                // Handle other client-side commands if necessary (exit, history, save shouldn't be needed here)
                if (commandName.equals("exit")) {
                    consoleReader.printError("'exit' command encountered in script. Halting script execution.");
                    // We don't exit the whole client, just this script
                    break; // Exit the script reading loop
                }
                if (commandName.equals("history")) {
                    handleHistory(); // Show history state mid-script
                    continue;
                }
                if (commandName.equals("save")) {
                    consoleReader.printError("'save' command is not allowed in scripts.");
                    continue;
                }


                // Prepare and send request for server-side commands
                Request request = parseAndPrepareRequest(commandName, argumentString);
                if (request != null) {
                    addToHistory(commandName); // Add script command to history
                    sendRequest(request);
                    processResponse(); // Wait for and process response before next script line
                } else {
                    // parseAndPrepareRequest already printed an error
                    consoleReader.printError("Halting script due to error preparing request for command: " + commandName);
                    break; // Stop script if request creation fails
                }
            } // End while loop

        } catch (FileNotFoundException e) {
            consoleReader.printError("Script file not found during execution: " + fileName);
        } catch (SecurityException e) {
            consoleReader.printError("Permission error reading script: " + fileName);
        } catch (Exception e) {
            consoleReader.printError("Unexpected error during script execution: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scriptStack.remove(canonicalPath); // IMPORTANT: Remove from stack when done/error
            consoleReader.println("--- Finished script: " + fileName + " ---");
        }
    }


    /** Serializes request and sends via NIO DatagramChannel. */
    private void sendRequest(Request request) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(request); // Serialize the request object
            byte[] data = bos.toByteArray();

            if (data.length > BUFFER_SIZE - 50) { // Check size (leave buffer room)
                consoleReader.printError("Request data is too large (" + data.length + " bytes). Cannot send via UDP.");
                return;
            }

            ByteBuffer buffer = ByteBuffer.wrap(data); // Wrap byte array in NIO buffer

            // Resolve server address each time (can be cached)
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

            // Send the datagram packet using the channel
            int bytesSent = channel.send(buffer, serverAddress);
            System.out.println("[Client] Sent request (" + request.getCommandName() + "), " + bytesSent + " bytes.");

        } catch (IOException e) {
            consoleReader.printError("Failed to serialize or send request: " + e.getMessage());
        }
    }

    /** Uses Selector for non-blocking receive with timeout. */
    private void processResponse() {
        try {
            // Clear previous selection results
            selector.selectNow(); // Non-blocking check first (optional)

            // Wait for channel to be ready or timeout
            consoleReader.print("Waiting for server response... ");
            int readyChannels = selector.select(SERVER_RESPONSE_TIMEOUT_MS); // Blocks up to timeout

            if (readyChannels == 0) {
                consoleReader.println("Timeout!"); // Print timeout on same line
                consoleReader.printError("No response received from server within " + (SERVER_RESPONSE_TIMEOUT_MS / 1000.0) + " seconds.");
                return;
            }
            consoleReader.println("Response received!"); // Print on same line if successful

            // Get the keys for ready channels
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isReadable()) {
                    ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    // Receive data into the buffer
                    SocketAddress serverAddress = channel.receive(receiveBuffer);

                    if (serverAddress != null) {
                        receiveBuffer.flip(); // Prepare buffer for reading
                        byte[] data = new byte[receiveBuffer.limit()];
                        receiveBuffer.get(data); // Copy data from buffer

                        // Deserialize
                        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                             ObjectInputStream ois = new ObjectInputStream(bis)) {
                            Response response = (Response) ois.readObject();
                            displayResponse(response); // Display the deserialized response
                        } catch (IOException | ClassNotFoundException | ClassCastException e) {
                            consoleReader.printError("Failed to deserialize/process server response: " + e.getMessage());
                            // e.printStackTrace(); // For debugging
                        }
                    } else {
                        consoleReader.printError("Received null address from server (channel closed?).");
                    }
                }
                keyIterator.remove(); // IMPORTANT: Remove key after processing
            }
        } catch (IOException e) {
            consoleReader.printError("Network error during response processing: " + e.getMessage());
        }
    }

    /** Displays the received response based on StatusCode and responseBody type. */
    private void displayResponse(Response response) {
        consoleReader.println("\n<<< Server Response <<<"); // Changed delimiters
        // System.out.println("DEBUG: " + response.toString()); // Optional raw debug

        if (response.isSuccess()) { // Status is OK
            Object body = response.getResponseBody();
            if (body instanceof String) {
                // For commands returning simple messages or formatted strings (info, simple results)
                consoleReader.println((String) body);
            } else if (body instanceof List) {
                // For commands returning lists (show, print_ascending, print_field_*)
                List<?> results = (List<?>) body;
                if (results.isEmpty()) {
                    consoleReader.println("(Collection is empty)");
                } else {
                    consoleReader.println("--- Result (" + results.size() + " items) ---");
                    for (Object item : results) {
                        consoleReader.println("  " + item.toString()); // Use item's toString
                    }
                    consoleReader.println("----------------------");
                }
            } else if (body != null) {
                // Handle other potential Serializable return types if needed
                consoleReader.println("Received data of type: " + body.getClass().getSimpleName());
                consoleReader.println(body.toString()); // Default toString
            } else {
                // OK status but no specific body (e.g., maybe for 'clear', 'add', 'remove')
                consoleReader.println("(Operation completed successfully)");
            }
        } else { // Status is ERROR or other failure code
            consoleReader.printError("Server reported error: " + response.getStatusCode());
            if (response.getErrorMessage() != null && !response.getErrorMessage().isEmpty()) {
                consoleReader.printError("Message: " + response.getErrorMessage());
            } else {
                consoleReader.printError("(No specific error message provided by server)");
            }
        }
        consoleReader.println(">>> End Response >>>");
    }


    /** Closes network resources. */
    private void closeResources() {
        try {
            if (selector != null && selector.isOpen()) selector.close();
            if (channel != null && channel.isOpen()) channel.close();
            System.out.println("\n[Client] Network resources closed.");
        } catch (IOException e) {
            consoleReader.printError("Error closing network resources: " + e.getMessage());
        }
    }

    // --- Main Method to Start Client ---
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java org.example.client.ClientApp <server_host> <server_port>");
            System.exit(1);
        }
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
            if (port <= 0 || port > 65535) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[1] + ". Must be 1-65535.");
            System.exit(1);
            return;
        }

        ClientApp client = new ClientApp(host, port);
        client.run();
    }
}