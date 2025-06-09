package org.example.server.core;

import org.example.common.network.Response;
import org.example.server.network.ResponseWithAddress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FutureManager {
    private static final List<Future<ResponseWithAddress>> futures = new CopyOnWriteArrayList<>();
    private static final List<ResponseWithAddress> immediateResponses = new CopyOnWriteArrayList<>();
    private static final Logger logger = Logger.getLogger(FutureManager.class.getName());

    public static void addFuture(Future<Response> future, SocketAddress clientAddress) {
        Future<ResponseWithAddress> wrappedFuture = new FutureWrapper(future, clientAddress);
        futures.add(wrappedFuture);
    }

    public static void addImmediateResponse(ResponseWithAddress responseWithAddress) {
        immediateResponses.add(responseWithAddress);
    }

    public static void checkAllFutures(DatagramChannel datagramChannel) {
        for (Future<ResponseWithAddress> future : futures) {
            if (future.isDone()) {
                try {
                    ResponseWithAddress result = future.get();
                    sendResponse(datagramChannel, result);
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "Error handling future: ", e);
                } finally {
                    futures.remove(future);
                }
            }
        }

        // Handle immediate responses
        for (ResponseWithAddress response : immediateResponses) {
            sendResponse(datagramChannel, response);
            immediateResponses.remove(response);
        }
    }

    private static void sendResponse(DatagramChannel datagramChannel, ResponseWithAddress result) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(result.getResponse());
            oos.flush();

            byte[] responseBytes = baos.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(responseBytes);

            datagramChannel.send(buffer, result.getClientAddress());
            logger.info("Sent response to " + result.getClientAddress());
        } catch (IOException e) {
            logger.severe("Failed to send response: " + e.getMessage());
        }
    }

    private static class FutureWrapper implements Future<ResponseWithAddress> {
        private final Future<Response> inner;
        private final SocketAddress address;

        public FutureWrapper(Future<Response> inner, SocketAddress address) {
            this.inner = inner;
            this.address = address;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return inner.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return inner.isCancelled();
        }

        @Override
        public boolean isDone() {
            return inner.isDone();
        }

        @Override
        public ResponseWithAddress get() throws InterruptedException, ExecutionException {
            return new ResponseWithAddress(inner.get(), address);
        }

        @Override
        public ResponseWithAddress get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return new ResponseWithAddress(inner.get(timeout, unit), address);
        }
    }
}


