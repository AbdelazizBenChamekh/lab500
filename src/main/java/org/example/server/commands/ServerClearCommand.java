package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.network.DatabaseHandler;

import java.util.List;

/**
 * Команда 'clear'
 * Очищает коллекцию
 */
public class ServerClearCommand extends Command implements CollectionEditor {
    private final CollectionManager collectionManager;

    public ServerClearCommand(CollectionManager collectionManager) {
        super("clear", ": очистить коллекцию");
        this.collectionManager = collectionManager;
    }

    /**
     * Исполнить команду
     * @param request аргументы команды
     * @throws IllegalArguments неверные аргументы команды
     */
    @Override
    public Response execute(Request request) throws IllegalArguments {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();

        // Get the list of user's StudyGroups
        List<StudyGroup> userGroups = collectionManager.getCollection().stream()
                .filter(studyGroup -> studyGroup.getUserLogin().equals(request.getUser().name()))
                .toList();

        // Extract their IDs
        List<Integer> deletedIds = userGroups.stream()
                .map(StudyGroup::getId)
                .toList();

        // Try to delete from DB
        if (DatabaseHandler.getDatabaseManager().deleteAllObjects(request.getUser(), deletedIds)) {
            // Remove from in-memory collection
            collectionManager.removeElements(userGroups);
            return new Response(StatusCode.OK, "Ваши элементы удалены");
        }

        return new Response(StatusCode.ERROR, "Элементы коллекции удалить не удалось");
    }
}
