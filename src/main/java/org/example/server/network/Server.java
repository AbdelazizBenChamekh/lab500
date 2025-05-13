package org.example.server.network;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.server.utility.ObjectSerializer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final int BUFFER_SIZE = 8192;

    private final int port;
    private final RequestHandler requestHandler;
    private final Logger logger;
    private DatagramSocket socket;
    private volatile boolean running = false;

    public Server(int port, RequestHandler requestHandler, Logger logger) {
        this.port = port;
        this.requestHandler = requestHandler;
        this.logger = logger;
    }

    public void run() {
        try {
            socket = new DatagramSocket(port);
            running = true;
            logger.info("Server socket created. Listening on UDP port: " + port);

            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            while (running) {
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                InetAddress clientAddress = null;
                int clientPort = -1;

                try {
                    logger.log(Level.FINE, "Waiting for next client request...");
                    socket.receive(receivePacket);

                    if (!running) break;

                    clientAddress = receivePacket.getAddress();
                    clientPort = receivePacket.getPort();
                    logger.info("Received packet from " + clientAddress.getHostAddress() + ":" + clientPort + " (" + receivePacket.getLength() + " bytes)");

                    Request request = null;
                    try {
                        Object deserializedObject = ObjectSerializer.deserialize(receivePacket.getData(), receivePacket.getLength());
                        if (deserializedObject instanceof Request) {
                            request = (Request) deserializedObject;
                            logger.fine("Deserialized request (Command: " + request.getCommandName() + ")");
                        } else {
                            logger.warning("Deserialized object is not a Request from " + clientAddress.getHostAddress());
                            sendRawResponse(Response.error(StatusCode.ERROR, "Bad request object type."), clientAddress, clientPort);
                            continue;
                        }
                    } catch (IOException | ClassNotFoundException | RuntimeException e) {
                        logger.log(Level.WARNING, "Deserialization failed from " + clientAddress.getHostAddress() + ": " + e.getMessage());
                        sendRawResponse(Response.error(StatusCode.ERROR, "Bad request format/data. Could not deserialize."), clientAddress, clientPort);
                        continue;
                    }

                    Response responseObject = requestHandler.handle(request);
                    sendRawResponse(responseObject, clientAddress, clientPort);

                } catch (SocketException se) {
                    if (running) {
                        logger.log(Level.WARNING, "SocketException during receive: " + se.getMessage());
                    } else {
                        logger.info("Socket closed while waiting for receive. Server stopping.");
                    }
                    running = false;
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Network I/O error in loop: " + e.getMessage(), e);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unexpected error handling request from " +
                            (clientAddress != null ? clientAddress.getHostAddress() : "unknown"), e);
                    if (clientAddress != null && clientPort != -1 && socket != null && !socket.isClosed()) {
                        sendRawResponse(Response.error(StatusCode.ERROR_SERVER, "Internal server error occurred."),
                                clientAddress, clientPort);
                    }
                }
            }
        } catch (SocketException e) {
            logger.log(Level.SEVERE, "FATAL: Could not create or bind server socket on port " + port + ".", e);
            System.err.println("FATAL ERROR: Could not bind to port " + port + ".");
        } finally {
            close();
        }
    }

    private void sendRawResponse(Response responseObject, InetAddress clientAddress, int clientPort) {
        if (socket == null || socket.isClosed()) {
            logger.warning("Cannot send response, socket is null or closed.");
            return;
        }
        try {
            byte[] sendData = ObjectSerializer.serialize(responseObject);

            if (sendData.length > BUFFER_SIZE) {
                logger.warning("Response size (" + sendData.length + " bytes) exceeds buffer limit. Sending size error response.");
                Response errorResponse = Response.error(StatusCode.ERROR_SERVER, "Server response data too large (limit: " + BUFFER_SIZE + " bytes).");
                sendData = ObjectSerializer.serialize(errorResponse);
                if (sendData.length > BUFFER_SIZE) {
                    logger.severe("INTERNAL ERROR: Size error response itself is too large. Cannot notify client.");
                    return;
                }
            }

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
            socket.send(sendPacket);
            logger.log(Level.FINE, "Sent response (Status: " + responseObject.getStatusCode() + ") to " + clientAddress.getHostAddress() + ":" + clientPort + " (" + sendData.length + " bytes)");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IOException during response serialization/send to " + clientAddress.getHostAddress(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error sending response", e);
        }
    }

    public void close() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
            logger.info("Server socket closed.");
        }
    }
}