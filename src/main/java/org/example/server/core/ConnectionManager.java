package org.example.server.core;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.network.RequestHandler;
import org.example.server.network.ResponseWithAddress;

import java.io.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConnectionManager implements Runnable {
    private final CommandManager commandManager;
    private final DatabaseManager databaseManager;
    private final DatagramChannel datagramChannel;
    private static final Logger connectionManagerLogger = Logger.getLogger(ConnectionManager.class.getName());

    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final ExecutorService requestExecutor = Executors.newCachedThreadPool();

    public ConnectionManager(CommandManager commandManager, DatabaseManager databaseManager, DatagramChannel datagramChannel) {
        this.commandManager = commandManager;
        this.databaseManager = databaseManager;
        this.datagramChannel = datagramChannel;
    }

    @Override
    public void run() {
        ByteBuffer buffer = ByteBuffer.allocate(65507); // Max UDP packet size

        // Thread to periodically check completed futures
        new Thread(() -> {
            while (true) {
                try {
                    FutureManager.checkAllFutures(datagramChannel);
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        while (true) {
            try {
                buffer.clear();
                SocketAddress clientAddress = datagramChannel.receive(buffer);
                if (clientAddress == null) {
                    continue;
                }
                buffer.flip();

                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);

                requestExecutor.submit(() -> {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                         ObjectInputStream ois = new ObjectInputStream(bais)) {

                        Request userRequest = (Request) ois.readObject();
                        connectionManagerLogger.info("Received request: " + userRequest.getCommandName());

                        if (!databaseManager.confirmUser(userRequest.getUser()) && !userRequest.getCommandName().equals("register")) {
                            Response responseToUser = new Response(StatusCode.LOGIN_FAILED, "Invalid user!");
                            FutureManager.addImmediateResponse(new ResponseWithAddress(responseToUser, clientAddress));
                        } else {
                            Future<Response> future = forkJoinPool.submit(
                                    new RequestHandler(commandManager, userRequest, datagramChannel, clientAddress)
                            );

                            if (future.isDone()) {
                                try {
                                    Response response = future.get();
                                    FutureManager.addImmediateResponse(new ResponseWithAddress(response, clientAddress));
                                } catch (Exception e) {
                                    connectionManagerLogger.severe("Error sending immediate response: " + e.getMessage());
                                }
                            } else {
                                FutureManager.addFuture(future, clientAddress);
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        connectionManagerLogger.severe("Error handling client data: " + e.getMessage());
                    }
                });

            } catch (IOException e) {
                connectionManagerLogger.severe("Error receiving packet: " + e.getMessage());
            }
        }
    }
}


