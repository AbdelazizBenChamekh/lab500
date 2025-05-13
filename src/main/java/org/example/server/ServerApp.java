package org.example.server;

import org.example.server.core.CollectionManager;
import org.example.server.core.FileManager;
import org.example.server.network.RequestHandler;
import org.example.server.network.Server;

import java.io.IOException;
import java.util.logging.*;

public class ServerApp {

    private static final String LOG_FILE_NAME = "server_lab6.log";
    private static final Logger logger = Logger.getLogger(ServerApp.class.getName());

    static {
        try {
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers.length > 0 && handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }
            logger.setLevel(Level.INFO);

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.INFO);
            consoleHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(consoleHandler);

            FileHandler fileHandler = new FileHandler(LOG_FILE_NAME, true);
            fileHandler.setLevel(Level.FINE);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);

            logger.setUseParentHandlers(false);

            logger.info("SERVER LOGGER configured. Console Level: " + consoleHandler.getLevel() +
                    ", File Level: " + fileHandler.getLevel() + " (logging to " + LOG_FILE_NAME + ")");
        } catch (IOException | SecurityException e) {
            System.err.println("FATAL: Failed to configure logger: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private CollectionManager collectionManagerForShutdown;
    private Server networkServerForShutdown;

    private ServerApp() {}

    private boolean initialize(int port, String saveFilePath) {
        try {
            logger.info("Initializing server components...");
            FileManager fileManager = new FileManager(saveFilePath, logger);
            this.collectionManagerForShutdown = new CollectionManager(fileManager, logger);
            RequestHandler requestHandler = new RequestHandler(collectionManagerForShutdown, logger);
            this.networkServerForShutdown = new Server(port, requestHandler, logger);

            logger.info("Server components initialized successfully.");
            registerShutdownHook();
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Server component initialization failed.", e);
            return false;
        }
    }

    private void registerShutdownHook() {
        Thread shutdownThread = new Thread(() -> {
            logger.info("Shutdown signal received by ServerApp.");
            if (this.networkServerForShutdown != null) {
                logger.info("Telling network server to close...");
                this.networkServerForShutdown.close();
            }
            if (this.collectionManagerForShutdown != null) {
                logger.info("Saving collection on shutdown...");
                try {
                    this.collectionManagerForShutdown.saveCollection();
                    logger.info("Collection saved successfully on shutdown.");
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error saving collection during shutdown.", e);
                }
            } else {
                logger.warning("CollectionManager not initialized, cannot save on shutdown.");
            }
            logger.info("ServerApp shutdown sequence complete.");
        }, "ServerApp-ShutdownHook");
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        logger.info("Shutdown hook registered.");
    }

    private void startServer() {
        if (this.networkServerForShutdown != null) {
            logger.info("Starting server network listener...");
            this.networkServerForShutdown.run();
            logger.info("Server network listener has finished.");
        } else {
            logger.severe("Cannot start server: Server instance not initialized.");
        }
    }

    public static void main(String[] args) {
        logger.info("ServerApp main method started.");
        int port = 12345;
        String saveFilePath = null;
        final String ENV_VAR_NAME = "STUDY_GROUP_DATA_FILE";

        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
                if (port <= 0 || port > 65535) {
                    logger.warning("Invalid port number " + args[0] + ". Using default " + 12345);
                    port = 12345;
                } else {
                    logger.info("Using port from command line argument: " + port);
                }
            } catch (NumberFormatException e) {
                logger.warning("Invalid port number format '" + args[0] + "'. Using default " + 12345);
                port = 12345;
            }
        } else {
            logger.info("No port argument provided. Using default port: " + 12345);
        }

        if (args.length >= 2) {
            saveFilePath = args[1];
            logger.info("Using save file path from command line argument: " + saveFilePath);
        } else {
            saveFilePath = System.getenv(ENV_VAR_NAME);
            if (saveFilePath == null || saveFilePath.trim().isEmpty()) {
                logger.warning("Save file path not provided via arg or env var (" + ENV_VAR_NAME + ").");
            } else {
                saveFilePath = saveFilePath.trim();
                logger.info("Using save file path from env var " + ENV_VAR_NAME + ": " + saveFilePath);
            }
        }

        if (saveFilePath == null || saveFilePath.trim().isEmpty()){
            String errorMsg = "Server cannot start without a valid save file path.\nProvide path as second arg or set " + ENV_VAR_NAME + " env var.";
            logger.severe(errorMsg);
            System.err.println("FATAL ERROR: " + errorMsg);
            System.exit(1);
        }

        ServerApp app = new ServerApp();
        if (app.initialize(port, saveFilePath)) {
            app.startServer();
        } else {
            logger.severe("Application failed to initialize. Exiting.");
            System.exit(1);
        }
        logger.info("ServerApp main method finished.");
    }
}