package org.example.server;


import org.example.server.core.*;

import org.example.server.network.*;
import org.example.server.commands.*;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerApp extends Thread {
    //-------------------------------КОНФИГУРАЦИОННЫЕ ПЕРЕМЕННЫЕ-----------------------------------------
    public static final int CONNECTION_TIMEOUT = 60 * 1000;

    public static final String HASHING_ALGORITHM = "SHA-1";
    public static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/studs";
    public static final String DATABASE_URL_HELIOS = "jdbc:postgresql://pg:5432/studs";
    public static final String DATABASE_CONFIG_PATH = "C:\\Users\\Mega-Pc\\Desktop\\programming\\lab50\\dbconfig.cfg";

    //--------------------------------------------------------------------------------------------------

    public static int PORT = 12345;
    public static final Logger rootLogger = Logger.getLogger(ServerApp.class.getName());

    public static void main(String[] args) {
        rootLogger.info("--------------------------------------------------------------------");
        rootLogger.info("----------------------ЗАПУСК СЕРВЕРА--------------------------------");
        rootLogger.info("--------------------------------------------------------------------");

        if(args.length != 0){
            try{
                PORT = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                rootLogger.warning("Неверный формат порта, будет использован порт по умолчанию");
            }
        }

        CollectionManager collectionManager = new CollectionManager(DatabaseHandler.getDatabaseManager());

        CommandManager commandManager = new CommandManager(DatabaseHandler.getDatabaseManager());
        commandManager.addCommand(List.of(
                new ServerHistoryCommand(commandManager),
                new ServerHelpCommand(commandManager),
                new ServerExecuteScript(),
                new ServerExit(),
                new ServerRegisterCommand(DatabaseHandler.getDatabaseManager()),
                new ServerInfoCommand(collectionManager),
                new ServerShowCommand(collectionManager),
                new ServerAddCommand(collectionManager),
                new ServerUpdateCommand(collectionManager),
                new ServerRemoveByIdCommand(collectionManager),
                new ServerClearCommand(collectionManager),
                new Ping(),
                new ServerExecuteScript(),
                new ServerAddIfMinCommand(collectionManager),
                new ServerRemoveLowerCommand(collectionManager)

        ));
        Server server = new Server(commandManager, DatabaseHandler.getDatabaseManager());
        try {
            server.run();
        } catch (Exception e) {
            rootLogger.log(Level.SEVERE, "Ошибка при запуске сервера: " + e.getMessage(), e);
        }
    }
}
