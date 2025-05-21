package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CollectionManager;
import org.example.common.utility.ConsoleColors;

/**
 * 'info' command
 * Outputs information about the collection (type, initialization date, number of elements, etc.) to the standard output stream.
 */
public class ServerInfoCommand extends Command{
    private CollectionManager collectionManager;

    public ServerInfoCommand(CollectionManager collectionManager) {
        super("info", ": вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)");
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
        String lastInitTime = (collectionManager.getLastInitTime() == null)
                ? "В сессии коллекция не инициализирована"
                : collectionManager.getLastInitTime().toString();
        String lastSaveTime = (collectionManager.getLastSaveTime() == null)
                ? "В сессии коллекция не инициализирована "
                : collectionManager.getLastSaveTime().toString();
        String stringBuilder = "Сведения о коллекции: \n" +
                ConsoleColors.toColor("Тип: ", ConsoleColors.GREEN) + collectionManager.collectionType() + "\n" +
                ConsoleColors.toColor("Количество элементов: ", ConsoleColors.GREEN) + collectionManager.collectionSize() + "\n" +
                ConsoleColors.toColor("Дата последней инициализации: ", ConsoleColors.GREEN) + lastInitTime + "\n" +
                ConsoleColors.toColor("Дата последнего изменения: ", ConsoleColors.GREEN) + lastSaveTime + "\n";
        return new Response(StatusCode.OK, stringBuilder);
    }
}