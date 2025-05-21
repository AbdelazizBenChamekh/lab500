package org.example.server;

import org.example.common.utility.ConsoleColors;
import org.example.server.exceptions.ExitObliged;
import org.example.server.core.*;
import org.example.server.network.*;
import org.example.server.commands.*;

import java.util.List;
import java.util.logging.Logger;
/**
 * Main Server App
 */
public class ServerApp extends Thread {
    public static int PORT = 12345;
    public static final int CONNECTION_TIMEOUT = 60 * 1000;
    private static final Printable console = new BlankConsole();

    private static final Logger logger = Logger.getLogger(ServerApp.class.getName());

    public static void main(String[] args) {
        if (args.length != 0) {
            try {
                PORT = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        // Get file path from env or default
        String filePath = System.getenv("STUDY_GROUP_DATA_FILE");
        if (filePath == null || filePath.isEmpty()) {
            filePath = "data.csv";
        }

        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(filePath, logger);

        try {
            logger.info("Creating objects...");
            fileManager.findFile();
            fileManager.createObjects();
            logger.info("Objects created successfully.");
        } catch (ExitObliged e) {
            console.println(ConsoleColors.toColor("Goodbye!", ConsoleColors.YELLOW));
            logger.severe("Error during object creation time.");
            return;
        }

        CommandManager commandManager = new CommandManager(fileManager);
        commandManager.addCommand(List.of(
                new ServerHelpCommand(commandManager),
                new ServerInfoCommand(collectionManager),
                new ServerShowCommand(collectionManager),
                new ServerAddCommand(collectionManager),
                new ServerUpdateCommand(collectionManager),
                new ServerRemoveByIdCommand(collectionManager),
                new ServerClearCommand(collectionManager),
                new ServerExecuteScript(),
                new ServerExit(),
                new ServerAddIfMinCommand(collectionManager, logger),
                new ServerRemoveLowerCommand(collectionManager, logger),
                new ServerHistoryCommand(commandManager),
                new ServerRemoveAnyByFormCommand(collectionManager, logger),
                new ServerPrintFieldAscendingAdminCommand(collectionManager, logger),
                new ServerPrintAscendingCommand(collectionManager, logger)
        ));

        logger.info("Command manager created.");
        RequestHandler requestHandler = new RequestHandler(commandManager);
        logger.info("Request handler created.");
        Server server = new Server(PORT, CONNECTION_TIMEOUT, requestHandler, fileManager);
        logger.info("Server object created.");
        server.run();
    }}

