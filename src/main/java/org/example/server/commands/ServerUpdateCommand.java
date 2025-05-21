package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CollectionManager;

import java.util.Objects;

/**
 * Command 'update'
 * Updates the value of the collection element whose id is equal to the specified one
 */
public class ServerUpdateCommand extends Command implements CollectionEditor{
    private final CollectionManager collectionManager;

    public ServerUpdateCommand(CollectionManager collectionManager) {
        super("update", " id {element}: обновить значение элемента коллекции, id которого равен заданному");
        this.collectionManager = collectionManager;
    }

    /**
     * Execute command
     * @param request command arguments
     * @throws IllegalArguments invalid command arguments
     */
    @Override
    public Response execute(Request request) throws IllegalArguments{
        if (request.getArgs().isBlank()) throw new IllegalArguments();
        class NoSuchId extends RuntimeException{

        }
        try {
            int id = Integer.parseInt(request.getArgs().trim());
            if (!collectionManager.checkExist(id)) throw new NoSuchId();
            if (Objects.isNull(request.getObject())){
                return new Response(StatusCode.ASK_OBJECT, "Для команды " + this.getName() + " требуется объект");
            }
            collectionManager.editById(id, request.getObject());
            return new Response(StatusCode.OK, "Объект успешно обновлен");
        } catch (NoSuchId err) {
            return new Response(StatusCode.ERROR,"В коллекции нет элемента с таким id");
        } catch (NumberFormatException exception) {
            return new Response(StatusCode.ERROR,"id должно быть числом типа int");
        }
    }
}