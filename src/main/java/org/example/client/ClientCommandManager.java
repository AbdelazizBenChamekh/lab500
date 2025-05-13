package org.example.client;

import org.example.client.commands.*;
import java.util.*;

public class ClientCommandManager {
    private final Map<String, ClientCommand> commandMap;
    private final List<String> history;
    private static final int HISTORY_SIZE = 12;
    private final Set<String> scriptStack;
    private final ClientApp clientApp;

    public ClientCommandManager(ClientApp clientAppInstance) {
        this.commandMap = new LinkedHashMap<>();
        this.history = new LinkedList<>();
        this.scriptStack = new HashSet<>();
        this.clientApp = clientAppInstance;
        registerClientCommands();
    }

    private void registerClientCommands() {
        commandMap.put("help", new ClientHelpCommand(commandMap));
        commandMap.put("info", new ClientInfoCommand());
        commandMap.put("show", new ClientShowCommand());
        commandMap.put("add", new ClientAddCommand());
        commandMap.put("update", new ClientUpdateCommand());
        commandMap.put("remove_by_id", new ClientRemoveByIdCommand());
        commandMap.put("clear", new ClientClearCommand());
        commandMap.put("add_if_min", new ClientAddIfMinCommand());
        commandMap.put("remove_lower", new ClientRemoveLowerCommand());
        commandMap.put("history", new ClientHistoryCommand(history));
        commandMap.put("remove_any_by_form_of_education", new ClientRemoveAnyByFormCommand());
        commandMap.put("print_ascending", new ClientPrintAscendingCommand());
        commandMap.put("print_field_ascending_group_admin", new ClientPrintFieldAscendingAdminCommand());
        commandMap.put("execute_script", new ClientExecuteScriptCommand(commandMap, history, scriptStack, clientApp));

        System.out.println("[Client] Registered " + commandMap.size() + " client commands in CommandManager.");
    }

    public ClientCommand getCommand(String commandName) {
        return commandMap.get(commandName.toLowerCase());
    }

    public void addToHistory(String commandName) {
        if (commandName == null || commandName.trim().isEmpty()) return;
        if (history.size() >= HISTORY_SIZE) {
            history.remove(0);
        }
        history.add(commandName.toLowerCase());
    }
}
