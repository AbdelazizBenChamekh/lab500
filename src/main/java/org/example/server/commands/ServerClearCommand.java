package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CollectionManager;

/**
 * Command 'clear'
 * Clears the collection
 */
public class ServerClearCommand extends Command implements CollectionEditor{
    private CollectionManager collectionManager;

    public ServerClearCommand(CollectionManager collectionManager) {
        super("clear", ": очистить коллекцию");
        this.collectionManager = collectionManager;
    }

    /**
     * Execute command
     * @param request command arguments
     * @throws IllegalArguments invalid command arguments
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();
        collectionManager.clear();
        return new Response(StatusCode.OK,"Элементы удалены");
    }
}