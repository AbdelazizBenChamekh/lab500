package org.example.server.commands;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.core.CollectionManager;
import org.example.common.models.StudyGroup;

import java.util.Collection;

/**
 * 'show' command
 * Prints all elements of the collection to standard output in string form
 */
public class ServerShowCommand extends Command{
    private CollectionManager collectionManager;

    public ServerShowCommand(CollectionManager collectionManager) {
        super("show", ": вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
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
        Collection<StudyGroup> collection = collectionManager.getCollection();
        if (collection == null || collection.isEmpty()) {
            return new Response(StatusCode.ERROR,"Коллекция еще не инициализирована");
        }
        return new Response(StatusCode.OK, "Коллекция: ", collection);
    }
}