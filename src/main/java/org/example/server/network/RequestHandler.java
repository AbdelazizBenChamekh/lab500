package org.example.server.network;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.core.CommandManager;
import org.example.server.exceptions.CommandRuntimeError;
import org.example.server.exceptions.ExitObliged;
import org.example.server.exceptions.IllegalArguments;
import org.example.server.exceptions.NoSuchCommand;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Callable;

public class RequestHandler implements Callable<Response> {
    private final CommandManager commandManager;
    private final Request request;
    private final DatagramChannel channel;
    private final SocketAddress clientAddress;

    public RequestHandler(CommandManager commandManager, Request request, DatagramChannel channel, SocketAddress clientAddress) {
        this.commandManager = commandManager;
        this.request = request;
        this.channel = channel;
        this.clientAddress = clientAddress;
    }

    @Override
    public Response call() {
        Response response;
        try {
            commandManager.addToHistory(String.valueOf(request.getUser()), request.getCommandName());
            response = commandManager.execute(request);
        } catch (IllegalArguments e) {
            response = new Response(StatusCode.WRONG_ARGUMENTS, "Неверное использование аргументов команды");
        } catch (CommandRuntimeError e) {
            response = new Response(StatusCode.ERROR, "Ошибка при исполнении программы");
        } catch (NoSuchCommand e) {
            response = new Response(StatusCode.ERROR, "Такой команды нет в списке");
        } catch (ExitObliged e) {
            response = new Response(StatusCode.EXIT);
        }

        sendResponse(response);
        return response;
    }

    private void sendResponse(Response response) {
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(byteStream);
            objectOut.writeObject(response);
            objectOut.flush();

            byte[] data = byteStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(data);

            channel.send(buffer, clientAddress);
        } catch (Exception e) {
            System.err.println("Ошибка при отправке ответа клиенту: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

