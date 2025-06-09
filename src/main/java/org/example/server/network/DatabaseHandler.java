package org.example.server.network;

import org.example.server.core.DatabaseManager;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHandler {
    private static DatabaseManager databaseManager;
    private static final Logger logger = Logger.getLogger(DatabaseHandler.class.getName());

    static {
        try {
            logger.info("DatabaseHandler static block: Initializing DatabaseManager instance...");
            databaseManager = new DatabaseManager();
            logger.info("DatabaseHandler static block: DatabaseManager instance created successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "FATAL: Failed to initialize DatabaseManager in DatabaseHandler static block.", e);
            System.err.println("FATAL: DatabaseManager initialization failed. Server cannot start. Check logs.");
            System.exit(1);
        }
    }

    public static DatabaseManager getDatabaseManager(){
        if (Objects.isNull(databaseManager)) {
            logger.severe("CRITICAL ERROR: getDatabaseManager() called but DatabaseManager instance is null.");
            throw new IllegalStateException("DatabaseManager not initialized or initialization failed.");
        }
        return databaseManager;
    }
}