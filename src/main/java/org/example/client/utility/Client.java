package org.example.client.utility;

import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.client.commandLine.Printable;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class Client {
    private final String host;
    private final int port;
    private final int reconnectionTimeout;
    private final int maxReconnectionAttempts;
    private final Printable console;

    private DatagramChannel channel;
    private Selector selector;

    private static final int BUFFER_SIZE = 65536; // 64 KB
    private static final int TIMEOUT_MS = 5000;   // 5 seconds timeout

    private int reconnectionAttempts = 0;

    public Client(String host, int port, int reconnectionTimeout, int maxReconnectionAttempts, Printable console) {
        this.host = host;
        this.port = port;
        this.reconnectionTimeout = reconnectionTimeout;
        this.maxReconnectionAttempts = maxReconnectionAttempts;
        this.console = console;
    }

    public Response sendAndAskResponse(Request request) throws IOException, ClassNotFoundException {
        if (request.isEmpty()) {
            return new Response(StatusCode.WRONG_ARGUMENTS, "Запрос пустой!");
        }

        openChannel();

        InetSocketAddress serverAddress = new InetSocketAddress(host, port);

        // Serialize request
        ByteBuffer sendBuffer = serialize(request);
        if (sendBuffer == null) {
            throw new IOException("Failed to serialize request");
        }

        // Send request
        channel.send(sendBuffer, serverAddress);

        ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);

        long startTime = System.currentTimeMillis();

        while (true) {
            int readyChannels = selector.select(TIMEOUT_MS);
            if (readyChannels == 0) {
                throw new IOException("Timeout waiting for server response");
            }

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isReadable()) {
                    receiveBuffer.clear();
                    InetSocketAddress fromAddress = (InetSocketAddress) channel.receive(receiveBuffer);
                    receiveBuffer.flip();

                    if (fromAddress != null && fromAddress.equals(serverAddress)) {
                        Response response = deserialize(receiveBuffer);
                        reconnectionAttempts = 0;
                        return response;
                    }
                }
            }

            if (System.currentTimeMillis() - startTime > TIMEOUT_MS) {
                closeChannel();
                throw new IOException("Timeout waiting for server response");
            }
        }
    }

    private void openChannel() throws IOException {
        if (channel != null && channel.isOpen()) return;

        channel = DatagramChannel.open();
        channel.configureBlocking(false);

        channel.bind(new InetSocketAddress(0));
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_READ);
    }



    private void closeChannel() throws IOException {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        if (selector != null) {
            selector.close();
            selector = null;
        }
    }

    private ByteBuffer serialize(Request request) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(request);
            oos.flush();
            byte[] bytes = baos.toByteArray();
            return ByteBuffer.wrap(bytes);
        } catch (IOException e) {
            console.printError("Serialization error: " + e.getMessage());
            return null;
        }
    }

    private Response deserialize(ByteBuffer buffer) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.array(), 0, buffer.limit()))) {
            return (Response) ois.readObject();
        }
    }
}
