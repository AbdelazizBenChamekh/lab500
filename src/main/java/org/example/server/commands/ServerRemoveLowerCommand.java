package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.ExceptionInFileMode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CollectionManager;
import org.example.common.models.StudyGroup;
import org.example.server.network.DatabaseHandler;

import java.util.Collection;
import java.util.Objects;

/**
 * Команда 'remove_lower'
 * Удаляет из коллекции все элементы, меньшие заданного
 */
public class ServerRemoveLowerCommand extends Command implements CollectionEditor {
    private final CollectionManager collectionManager;

    public ServerRemoveLowerCommand(CollectionManager collectionManager) {
        super("remove_lower", " {element} : удалить из коллекции все элементы, меньшие заданного");
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

        class NoElements extends RuntimeException {}

        try {
            if (Objects.isNull(request.getObject())) {
                return new Response(StatusCode.ASK_OBJECT, "Для команды " + this.getName() + " требуется объект");
            }

            Collection<StudyGroup> toRemove = collectionManager.getCollection().stream()
                    .filter(Objects::nonNull)
                    .filter(studyGroup -> studyGroup.compareTo(request.getObject()) <= -1) // changed to <= -1
                    .filter(studyGroup -> studyGroup.getUserLogin().equals(request.getUser().name()))
                    .filter(obj -> DatabaseHandler.getDatabaseManager().deleteObject(obj.getId(), request.getUser()))
                    .toList();

            collectionManager.removeElements(toRemove);
            return new Response(StatusCode.OK, "Удалены элементы меньшие чем заданный");

        } catch (NoElements e) {
            return new Response(StatusCode.ERROR, "В коллекции нет элементов");
        } catch (ExceptionInFileMode e) {
            return new Response(StatusCode.ERROR, "Поля в файле не валидны! Объект не создан");
        }
    }
}
