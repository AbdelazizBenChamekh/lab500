package org.example.server.network;


import org.example.common.models.StudyGroup;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.server.exceptions.ConnectionErrorException;
import org.example.server.exceptions.OpeningServerException;
import org.example.server.core.FileManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashSet;

public class Server {
    private int port;
    private int soTimeout;
    private Printable console;
    private ServerSocketChannel ss;
    private SocketChannel socketChannel;
    private RequestHandler requestHandler;
    private LinkedHashSet<StudyGroup> collection;


    BufferedInputStream bf = new BufferedInputStream(System.in);
    BufferedReader scanner = new BufferedReader(new InputStreamReader(bf));
    private FileManager fileManager;

    public Server(int port, int soTimeout, RequestHandler handler, FileManager fileManager) {
        this.port = port;
        this.soTimeout = soTimeout;
        this.console = new BlankConsole();
        this.requestHandler = handler;
        this.fileManager = fileManager;
        this.collection = fileManager.loadCollection();
    }

    public void run() {
        try {
            openServerSocket();
            System.out.println("Server socket opened on port " + port);
            while (true) {
                try {
                    if (scanner.ready()) {
                        String line = scanner.readLine();
                        if (line.equals("save") || line.equals("s")) {
                            fileManager.saveCollection(collection);
                            System.out.println("Collection saved to file");
                        }
                    }
                } catch (IOException ignored) {}

                try (SocketChannel clientSocket = connectToClient()) {
                    if (clientSocket == null) continue;
                    clientSocket.configureBlocking(false);
                    if (!processClientRequest(clientSocket)) break;
                } catch (ConnectionErrorException | SocketTimeoutException ignored) {
                    // Ignored as per original logic
                } catch (IOException exception) {
                    console.printError("Error while closing client connection!");
                    System.err.println("Error while closing client connection: " + exception.getMessage());
                }
            }
            stop();
            System.out.println("Server stopped");
        } catch (OpeningServerException e) {
            console.printError("Server could not be started");
            System.err.println("Server could not be started");
        }
    }

    private void openServerSocket() throws OpeningServerException {
        try {
            SocketAddress socketAddress = new InetSocketAddress(port);
            System.out.println("Creating server socket on port " + port);
            ss = ServerSocketChannel.open();
            ss.bind(socketAddress);
            ss.configureBlocking(false);
            System.out.println("Server socket channel configured (non-blocking)");
        } catch (IllegalArgumentException exception) {
            console.printError("Port '" + port + "' is out of valid range!");
            System.err.println("Port " + port + " is out of valid range");
            throw new OpeningServerException();
        } catch (IOException exception) {
            System.err.println("Error using port " + port + ": " + exception.getMessage());
            console.printError("Error using port '" + port + "'!");
            throw new OpeningServerException();
        }
    }

    private SocketChannel connectToClient() throws ConnectionErrorException, SocketTimeoutException {
        try {
            socketChannel = ss.accept();
            if (socketChannel != null) {
                System.out.println("Client connected: " + socketChannel.getRemoteAddress());
            }
            return socketChannel;
        } catch (SocketTimeoutException exception) {
            throw new SocketTimeoutException();
        } catch (IOException exception) {
            System.err.println("Error connecting to client: " + exception.getMessage());
            throw new ConnectionErrorException();
        }
    }

    private boolean processClientRequest(SocketChannel clientSocket) {
        Request userRequest = null;
        Response responseToUser = null;
        try {
            Request request = getSocketObjet(clientSocket);
            System.out.println("Received request: " + request.getCommandName());
            console.println(request.toString());
            responseToUser = requestHandler.handle(request);
            sendSocketObject(clientSocket, responseToUser);
            System.out.println("Sent response to client");
        } catch (ClassNotFoundException exception) {
            console.printError("Error reading incoming data!");
            System.err.println("Error reading incoming data: " + exception.getMessage());
        } catch (InvalidClassException | NotSerializableException exception) {
            console.printError("Error sending data to client!");
            System.err.println("Error sending data to client: " + exception.getMessage());
        } catch (IOException exception) {
            if (userRequest == null) {
                console.printError("Unexpected client disconnect!");
                System.err.println("Unexpected client disconnect");
            } else {
                console.println("Client disconnected successfully!");
                System.out.println("Client disconnected successfully");
            }
        }
        return true;
    }

    public static Request getSocketObjet(SocketChannel channel) throws IOException, ClassNotFoundException {
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 10);
        while (true) {
            try {
                channel.read(buffer);
                buffer.mark();
                byte[] buf = buffer.array();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf);
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                return (Request) objectInputStream.readObject();
            } catch (StreamCorruptedException e) {
                // try again if not enough data
            }
        }
    }

    private static void sendSocketObject(SocketChannel client, Response response) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(response);
        objectOutputStream.flush();
        client.write(ByteBuffer.wrap(byteArrayOutputStream.toByteArray()));
    }

    private void stop() {
        class ClosingSocketException extends Exception {}
        try {
            if (socketChannel == null) throw new ClosingSocketException();
            socketChannel.close();
            ss.close();
            System.out.println("All connections closed");
        } catch (ClosingSocketException exception) {
            console.printError("Cannot stop server that was not started!");
            System.err.println("Cannot stop server that was not started!");
        } catch (IOException exception) {
            console.printError("Error while stopping server!");
            System.err.println("Error while stopping server: " + exception.getMessage());
        }
    }
}
