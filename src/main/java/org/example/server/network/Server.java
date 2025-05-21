package org.example.server.network;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.server.core.FileManager;
import org.example.server.exceptions.OpeningServerException;
import org.example.client.commandLine.Printable;
import org.example.client.commandLine.BlankConsole;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
    private final int port;
    private final Printable console;
    private final FileManager fileManager;
    private final RequestHandler requestHandler;
    private DatagramSocket socket;
    private volatile boolean running = false;
    private static final int BUFFER_SIZE = 65536; // 64 KB buffer for UDP packets

    public Server(int port, RequestHandler handler, FileManager fileManager) {
        this.port = port;
        this.console = new BlankConsole();
        this.requestHandler = handler;
        this.fileManager = fileManager;
    }

    public void run() {
        try {
            openSocket();
            console.println("UDP server started on port " + port);
            running = true;

            while (running) {
                byte[] receiveBuffer = new byte[BUFFER_SIZE];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket); // blocking call

                // Deserialize request
                Request request = deserializeRequest(receivePacket.getData(), receivePacket.getLength());
                console.println("Received request: " + (request != null ? request.getCommandName() : "null"));

                // Handle request
                Response response = requestHandler.handle(request);

                // Serialize response
                byte[] responseBytes = serializeResponse(response);

                // Send response back to client
                DatagramPacket sendPacket = new DatagramPacket(
                        responseBytes,
                        responseBytes.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort()
                );
                socket.send(sendPacket);
                console.println("Sent response to client " + receivePacket.getAddress() + ":" + receivePacket.getPort());
            }
        } catch (IOException e) {
            console.printError("IO error in server: " + e.getMessage());
            e.printStackTrace();
        } catch (OpeningServerException e) {
            throw new RuntimeException(e);
        } finally {
            stop();
        }
    }

    private void openSocket() throws OpeningServerException {
        try {
            socket = new DatagramSocket(port);
            socket.setSoTimeout(0); // infinite timeout, can be adjusted
        } catch (IOException e) {
            console.printError("Could not open UDP socket on port " + port);
            throw new OpeningServerException();
        }
    }

    private Request deserializeRequest(byte[] data, int length) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data, 0, length);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (Request) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to deserialize Request object", e);
        }
    }

    private byte[] serializeResponse(Response response) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
            oos.flush();
            return baos.toByteArray();
        }
    }

    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        console.println("Server stopped");
    }
}
