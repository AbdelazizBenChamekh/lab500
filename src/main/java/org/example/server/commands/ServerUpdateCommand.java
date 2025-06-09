package org.example.server.commands;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CollectionManager;
import org.example.server.network.DatabaseHandler;

import java.util.Objects;

/**
 * Команда 'update'
 * Обновляет значение элемента коллекции, id которого равен заданному
 */
public class ServerUpdateCommand extends Command implements CollectionEditor{
    private final CollectionManager collectionManager;

    public ServerUpdateCommand(CollectionManager collectionManager) {
        super("update", " id {element}: обновить значение элемента коллекции, id которого равен заданному");
        this.collectionManager = collectionManager;
    }

    /**
     * Исполнить команду
     * @param request аргументы команды
     * @throws IllegalArguments неверные аргументы команды
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
            if(DatabaseHandler.getDatabaseManager().updateObject(id, request.getObject(), request.getUser())){
                collectionManager.editById(id, request.getObject());
                return new Response(StatusCode.OK, "Объект успешно обновлен");
            }
            return new Response(StatusCode.ERROR, "Объект не обновлен. Вероятнее всего он не ваш");
        } catch (NoSuchId err) {
            return new Response(StatusCode.ERROR,"В коллекции нет элемента с таким id");
        } catch (NumberFormatException exception) {
            return new Response(StatusCode.ERROR,"id должно быть числом типа int");
        }
    }
}