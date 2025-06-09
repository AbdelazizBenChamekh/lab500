package org.example.server.network;

import org.example.common.network.Response;

import java.net.SocketAddress;

public class ResponseWithAddress {
    private final Response response;
    private final SocketAddress clientAddress;

    public ResponseWithAddress(Response response, SocketAddress clientAddress) {
        this.response = response;
        this.clientAddress = clientAddress;
    }

    public Response getResponse() {
        return response;
    }

    public SocketAddress getClientAddress() {
        return clientAddress;
    }
}
