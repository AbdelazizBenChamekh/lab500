package org.example.client;

import org.example.common.network.Request;
import org.example.common.network.Response;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Manages network communication for the client using non-blocking NIO.
 * Handles sending requests and receiving responses.
 */
public class ClientNetworkManager implements AutoCloseable {
    private static final int SERVER_RESPONSE_TIMEOUT_MS = 5000;
    private static final int BUFFER_SIZE = 8192;

    private final String serverHost;
    private final int serverPort;
    private DatagramChannel channel;
    private Selector selector;
    private final ConsoleReader console;

    public ClientNetworkManager(String host, int port, ConsoleReader console) throws IOException {
        this.serverHost = host;
        this.serverPort = port;
        this.console = console;
        initializeNetwork();
    }

    private void initializeNetwork() throws IOException {
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
        console.println("[Client] Network channel initialized for server " + serverHost + ":" + serverPort);
    }

    /**
     * Sends a request to the server.
     * @param request The Request object to send.
     * @return true if sending was attempted without immediate IO error, false otherwise.
     */
    public boolean sendRequest(Request request) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(request);
            byte[] data = bos.toByteArray();

            if (data.length > BUFFER_SIZE - 50) {
                console.printError("Request data is too large (" + data.length + " bytes). Cannot send via UDP.");
                return false;
            }
            ByteBuffer buffer = ByteBuffer.wrap(data);
            InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);
            channel.send(buffer, serverAddress);
            // console.println("[Client] Sent request (" + request.getCommandName() + ")"); // Less verbose
            return true;
        } catch (IOException e) {
            console.printError("Failed to serialize or send request: " + e.getMessage());
            return false;
        }
    }

    /**
     * Waits for and receives a response from the server using the Selector.
     * @return The Response object, or null if timeout or error occurs.
     */
    public Response receiveResponse() {
        try {
            selector.selectNow(); // Clear any previous selection
            console.print("Waiting for server response... ");
            int readyChannels = selector.select(SERVER_RESPONSE_TIMEOUT_MS);

            if (readyChannels == 0) {
                console.println("Timeout!");
                console.printError("No response received from server within " + (SERVER_RESPONSE_TIMEOUT_MS / 1000.0) + " seconds.");
                return null;
            }
            console.println("Received!");

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            if (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isReadable()) {
                    ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                    SocketAddress serverAddress = channel.receive(receiveBuffer); // Read data

                    if (serverAddress != null) {
                        receiveBuffer.flip();
                        byte[] data = new byte[receiveBuffer.limit()];
                        receiveBuffer.get(data);

                        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                             ObjectInputStream ois = new ObjectInputStream(bis)) {
                            return (Response) ois.readObject();
                        } catch (IOException | ClassNotFoundException | ClassCastException e) {
                            console.printError("Failed to deserialize server response: " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    } else {
                        console.printError("Received null address from server (channel may have closed).");
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            console.printError("Network error during response processing: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void close() throws IOException {
        if (selector != null && selector.isOpen()) selector.close();
        if (channel != null && channel.isOpen()) channel.close();
        console.println("\n[Client] Network resources closed.");
    }
}
