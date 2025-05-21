package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CollectionManager;

import java.util.Objects;

/**
 * 'add' command
 * Adds a new element to the collection
 */
public class ServerAddCommand extends Command implements CollectionEditor{
    private final CollectionManager collectionManager;

    public ServerAddCommand(CollectionManager collectionManager) {
        super("add", " {element}: добавить новый элемент в коллекцию");
        this.collectionManager = collectionManager;
    }

    /**
     * Execute the command
     * @param request client request
     * @throws IllegalArguments invalid command arguments
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();
        if (Objects.isNull(request.getObject())){
            return new Response(StatusCode.ASK_OBJECT, "Для команды " + this.getName() + " требуется объект");
        } else{
            request.getObject().setId(CollectionManager.getNextId());
            collectionManager.addElement(request.getObject());
            return new Response(StatusCode.OK, "Объект успешно добавлен");
        }
    }
}
