package org.example.server.network;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.ServerApp;

import org.example.server.core.CommandManager;
import org.example.server.core.DatabaseManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private final int port;
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    private DatagramChannel channel;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Server(CommandManager commandManager, DatabaseManager databaseManager) {
        this.port = ServerApp.PORT;
        this.commandManager = commandManager;
        this.databaseManager = databaseManager;
    }

    public void run() {
        try {
            channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(port));
            channel.configureBlocking(true);

            logger.info("UDP сервер (DatagramChannel) запущен на порту " + port);

            while (true) {
                ByteBuffer buffer = ByteBuffer.allocate(65535); // Buffer for receiving data
                SocketAddress clientAddress = channel.receive(buffer);

                if (clientAddress != null) {
                    buffer.flip();
                    byte[] data = new byte[buffer.remaining()];
                    buffer.get(data);

                    executor.submit(() -> handleRequest(data, clientAddress));
                }
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Не удалось запустить сервер: " + e.getMessage(), e);
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Ошибка при закрытии DatagramChannel: " + e.getMessage(), e);
                }
            }
        }
    }

    private void handleRequest(byte[] data, SocketAddress clientAddress) {
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInput = new ObjectInputStream(byteStream)) {

            Request request = (Request) objectInput.readObject();

            RequestHandler handler = new RequestHandler(commandManager, request, channel, clientAddress);
            handler.call(); // Sends response internally

        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.WARNING, "Ошибка при обработке запроса: " + e.getMessage(), e);
            sendErrorResponse(clientAddress, "Ошибка при обработке запроса: " + e.getMessage());
        }
    }

    private void sendErrorResponse(SocketAddress address, String message) {
        try {
            Response response = new Response(StatusCode.ERROR, message);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(byteStream);
            objectOutput.writeObject(response);
            objectOutput.flush();

            byte[] respBytes = byteStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(respBytes);

            channel.send(buffer, address);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Ошибка при отправке сообщения об ошибке клиенту: " + e.getMessage(), e);
        }
    }
}
