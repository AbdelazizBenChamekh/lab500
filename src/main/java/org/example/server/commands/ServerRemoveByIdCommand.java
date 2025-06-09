package org.example.server.commands;



import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.network.DatabaseHandler;



/**
 * Команда 'remove_by_id'
 * Удаляет элемент из коллекции по его id
 */
public class ServerRemoveByIdCommand extends Command implements CollectionEditor{
    private final CollectionManager collectionManager;

    public ServerRemoveByIdCommand (CollectionManager collectionManager) {
        super("remove_by_id", " id: удалить элемент из коллекции по его id");
        this.collectionManager = collectionManager;
    }

    /**
     * Исполнить команду
     * @param request аргументы команды
     * @throws IllegalArguments неверные аргументы команды
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (request.getArgs().isBlank()) throw new IllegalArguments();
        class NoSuchId extends RuntimeException {
        }
        try {
            int id = Integer.parseInt(request.getArgs().trim());
            if (!collectionManager.checkExist(id)) throw new NoSuchId();
            if (DatabaseHandler.getDatabaseManager().deleteObject(id, request.getUser())) {
                collectionManager.removeElement(collectionManager.getById(id));
                return new Response(StatusCode.OK,"Объект удален успешно");
            } else{
                return new Response(StatusCode.ERROR, "Выбранный объект не удален. Скорее всего он вам не принадлежит");
            }
        } catch (NoSuchId err) {
            return new Response(StatusCode.ERROR,"В коллекции нет элемента с таким id");
        } catch (NumberFormatException exception) {
            return new Response(StatusCode.WRONG_ARGUMENTS,"id должно быть числом типа int");
        }
    }
}