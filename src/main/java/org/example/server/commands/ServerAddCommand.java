package org.example.server.commands;

import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CollectionManager;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.exceptions.InvalidForm;
import org.example.server.network.DatabaseHandler;

import java.util.Objects;

/**
 * Команда 'add'
 * Добавляет новый элемент в коллекцию
 */
public class ServerAddCommand extends Command implements CollectionEditor {
    private final CollectionManager collectionManager;

    public ServerAddCommand(CollectionManager collectionManager) {
        super("add", " {element}: добавить новый элемент в коллекцию");
        this.collectionManager = collectionManager;
    }

    /**
     * Исполнить команду
     *
     * @param request запрос клиента
     * @throws IllegalArguments неверные аргументы команды
     * @throws InvalidForm      ошибка формы данных
     */
    @Override
    public Response execute(Request request) throws IllegalArguments, InvalidForm {
        if (!request.getArgs().isBlank()) throw new IllegalArguments();

        if (Objects.isNull(request.getObject())) {
            return new Response(StatusCode.ASK_OBJECT, "Для команды " + this.getName() + " требуется объект");
        } else {
            validateStudyGroup(request.getObject());

            int newId = DatabaseHandler.getDatabaseManager().addObject(request.getObject(), request.getUser());
            if (newId == -1)
                return new Response(StatusCode.ERROR, "Объект добавить не удалось");

            request.getObject().setId(newId);
            request.getObject().setUserLogin(request.getUser().name());
            collectionManager.addElement(request.getObject());

            return new Response(StatusCode.OK, "Объект успешно добавлен");
        }
    }

    /**
     * Валидация объекта StudyGroup
     *
     * @param obj объект из запроса
     * @throws InvalidForm если объект некорректен
     */
    private void validateStudyGroup(Object obj) throws InvalidForm {
        if (!(obj instanceof StudyGroup group)) {
            throw new InvalidForm("Переданный объект не является группой.");
        }

        if (group.getName() == null || group.getName().isBlank()) {
            throw new InvalidForm("Имя группы не может быть пустым.");
        }

        if (group.getStudentsCount() <= 0) {
            throw new InvalidForm("Количество студентов должно быть положительным.");
        }

        if (group.getCoordinates() == null) {
            throw new InvalidForm("Координаты не могут быть пустыми.");
        }

        if (group.getGroupAdmin() == null) {
            throw new InvalidForm("Группа должна иметь администратора.");
        }

    }
}

