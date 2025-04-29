package org.example.server;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import org.example.server.core.FileManager;
import org.example.server.network.RequestHandler;
import org.example.client.ConsoleReader;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.*;

/**
 * Main application class for the server.
 * Listens for UDP requests, deserializes them, passes them to a handler,
 * serializes the response, and sends it back to the client, Includes logging
 */
public class ServerApp {

    private static final int BUFFER_SIZE = 8192; // Max expected UDP packet size for receiving/sending data
    private static final String LOG_FILE_NAME = "server_lab6.log";

    // Logger Setup
    private static final Logger logger = Logger.getLogger(ServerApp.class.getName());

    static {
        try {

            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers.length > 0 && handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }


            logger.setLevel(Level.INFO);

            // Console Handler
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO); // <<< Console handler also needs a level
            consoleHandler.setFormatter(new SimpleFormatter()); // Simple readable format

            // File Handler
            FileHandler fileHandler = new FileHandler(LOG_FILE_NAME, 0, 1, true);
            fileHandler.setLevel(Level.INFO); // <<< Log INFO and above to file
            fileHandler.setFormatter(new SimpleFormatter()); // Use simple format for file too (or XMLFormatter)

            // Add handlers to our logger
            logger.addHandler(consoleHandler);
            logger.addHandler(fileHandler);

            logger.setUseParentHandlers(false);

            logger.info("Logger configured. Console logging level: " + consoleHandler.getLevel() +
                    ", File logging level: " + fileHandler.getLevel() + " to " + LOG_FILE_NAME);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to configure file logging handler.", e);
        } catch (SecurityException e) {
            logger.log(Level.SEVERE, "Security exception configuring logger.", e);
        }
    }

    private final int port;
    private final ConsoleReader console; // For server's own logging/messages
    private final CollectionManager collectionManager;
    private final RequestHandler requestHandler;


    public ServerApp(int port, String saveFilePath) {
        this.port = port;
        this.console = new ConsoleReader(new java.util.Scanner(System.in));

        // Initialize managers
        FileManager fileManager = new FileManager(saveFilePath, console);
        this.collectionManager = new CollectionManager(fileManager, console);
        this.requestHandler = new RequestHandler(collectionManager, logger); // Give handler access

        console.println("Server components initialized.");
        registerShutdownHook(); // Register hook to save on exit
    }

    // Save collection on Ctrl+C or termination
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            console.println("\nShutdown signal received. Saving collection...");
            if (this.collectionManager != null) {
                this.collectionManager.saveCollection();
                console.println("Collection saved. Server exiting.");
            } else {
                console.printError("Cannot save collection on shutdown: CollectionManager not initialized.");
            }
        }));
        console.println("Shutdown hook registered.");
    }

    // Main listening and delegation loop
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
            console.println("Server socket created successfully on port: " + port);

            //Infinite Listening Loop
            while (true) {
                Request request = null;
                InetAddress clientAddress = null;
                int clientPort = -1;
                DatagramPacket receivePacket = null;

                try {
                    // *** 3. Prepare for Reception ***
                    byte[] receiveBuffer = new byte[8192];
                    receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                    console.println("Server waiting for request on port " + port + "...");

                    // *** 4. Receive Packet (BLOCKING) ***
                    socket.receive(receivePacket); // Program waits here

                    clientAddress = receivePacket.getAddress();
                    clientPort = receivePacket.getPort();
                    console.println("Received packet from " + clientAddress.getHostAddress() + ":" + clientPort + " (" + receivePacket.getLength() + " bytes)");

                    // *** 5. Deserialize Request ***
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 0, receivePacket.getLength());
                         ObjectInputStream ois = new ObjectInputStream(bais)) {
                        request = (Request) ois.readObject();
                        console.println("Deserialized request (Command: " + request.getCommandName() + ")");
                    } catch (Exception e) { // Catch broader errors during deserialization
                        console.printError("Deserialization error from " + clientAddress.getHostAddress() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
                        sendResponse(new Response(StatusCode.ERROR, null, "Bad request format/data."),
                                socket, clientAddress, clientPort);
                        continue;
                    }

                    // *** 6. Process Request (Delegate to Handler) ***
                    Response response = requestHandler.handle(request); // Get response from handler

                    // *** 7. Serialize and Send Response ***
                    sendResponse(response, socket, clientAddress, clientPort);

                } catch (IOException e) {
                    console.printError("Network I/O error (receive/send): " + e.getMessage());
                } catch (Exception e) {
                    // Catch unexpected errors during the handling of a single request cycle
                    console.printError("Unexpected error handling request from " +
                            (clientAddress != null ? clientAddress.getHostAddress() : "unknown") + ": " + e.getMessage());
                    e.printStackTrace();
                    // Try to send a generic error response if possible
                    if (clientAddress != null && clientPort != -1 && socket != null && !socket.isClosed()) {
                        sendResponse(new Response(StatusCode.ERROR_SERVER, null, "Internal server error occurred."),
                                socket, clientAddress, clientPort);
                    }
                }
            }

        } catch (SocketException e) {
            console.printError("FATAL: Could not create or bind server socket on port " + port + ": " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                console.println("Server socket closed.");
            }
        }
    }

    /**
     * Helper method to serialize and send a Response object back to the client.
     */
    private void sendResponse(Response response, DatagramSocket socket, InetAddress clientAddress, int clientPort) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(response);
            oos.flush();
            byte[] sendData = baos.toByteArray();


            if (sendData.length > 8000) { // Check against a reasonable UDP limit
                console.printError("ERROR: Response size (" + sendData.length + " bytes) is very large and might be dropped.");
                Response errorResponse = new Response(StatusCode.ERROR_SERVER, null, "Response data too large to send reliably via UDP.");
                try (ByteArrayOutputStream errorBaos = new ByteArrayOutputStream(); ObjectOutputStream errorOos = new ObjectOutputStream(errorBaos)) {
                    errorOos.writeObject(errorResponse);
                    sendData = errorBaos.toByteArray();
                }
            }

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            socket.send(sendPacket);
            console.println("Sent response (Status: " + response.getStatusCode() + ") to " + clientAddress.getHostAddress() + ":" + clientPort + " (" + sendData.length + " bytes)");

        } catch (IOException e) {
            console.printError("IOException during response serialization/send to " + clientAddress.getHostAddress() + ": " + e.getMessage());
        } catch (Exception e) { // Catch unexpected errors during sending
            console.printError("Unexpected error sending response: " + e.getMessage());
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        int port = 12345;
        String saveFilePath = null;
        final String ENV_VAR_NAME = "STUDY_GROUP_DATA_FILE";

        // --- Argument Parsing
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
                if (port <= 0 || port > 65535) {
                    System.err.println("Warning: Invalid port number " + args[0] + ". Using default " + port);
                    port = 12345;
                }
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid port number format '" + args[0] + "'. Using default " + port);
            }
        }
        if (args.length >= 2) {
            saveFilePath = args[1];
        } else {
            saveFilePath = System.getenv(ENV_VAR_NAME);
            if (saveFilePath == null || saveFilePath.trim().isEmpty()) {
                System.err.println("Warning: Save file path not provided via args or environment variable (" + ENV_VAR_NAME + ").");
            }
        }


        // --- Path Validation ---
        if (saveFilePath == null || saveFilePath.trim().isEmpty()){
            System.err.println("FATAL ERROR: Server cannot start without a valid save file path.");
            System.err.println("Provide path as second argument or set " + ENV_VAR_NAME + " environment variable.");
            System.exit(1); // Exit if no path configured
        }


        ServerApp server = new ServerApp(port, saveFilePath.trim());
        server.run(); // Starts the listening loop
    }
}
