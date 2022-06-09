/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.neohub.internal;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Handles the ASCII based communication via web socket between openHAB and NeoHub
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
@WebSocket
public class NeoHubWebSocket extends NeoHubSocketBase {

    private static final int SLEEP_MILLISECONDS = 100;

    private final Logger logger = LoggerFactory.getLogger(NeoHubWebSocket.class);

    private final WebSocketClient webSocketClient;
    private @Nullable Session session = null;
    private String responseJson = "";
    private boolean responseReceived;
    private final Gson gson = new Gson();

    public NeoHubWebSocket(NeoHubConfiguration config) {
        super(config);

        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);
        HttpClient httpClient = new HttpClient(sslContextFactory);
        try {
            httpClient.start();
        } catch (Exception e) {
        }
        webSocketClient = new WebSocketClient(httpClient);

        try {
            webSocketClient.setConnectTimeout(config.socketTimeout * 1000);
            webSocketClient.start();
        } catch (Exception e) {
            logger.debug("Error starting web socket client: '{}'", e.getMessage());
            return;
        }
    }

    /**
     * Open the web socket session.
     *
     * @throws NeoHubException
     */
    private void openSession() throws NeoHubException {
        Session session = this.session;
        if (session == null || !session.isOpen()) {
            closeSession();
            try {
                URI uri = new URI(String.format("wss://%s:%d", config.hostName, config.portNumber));
                webSocketClient.connect(this, uri).get();
            } catch (InterruptedException | ExecutionException | IOException | URISyntaxException e) {
                throw new NeoHubException(String.format("Error opening session: '%s'", e.getMessage()));
            }
        }
    }

    /**
     * Close the web socket session.
     */
    private void closeSession() {
        Session session = this.session;
        if (session != null) {
            session.close();
            this.session = null;
        }
    }

    @Override
    public String sendMessage(final String requestJson) throws IOException, NeoHubException {
        // start the session
        openSession();

        Session session = this.session;
        if (session == null) {
            throw new NeoHubException("Session is null.");
        }

        // clear the response
        responseJson = "";
        responseReceived = false;

        // create and send the request
        NeohubWebSocketRequest requestDto = new NeohubWebSocketRequest();
        requestDto.setFields(config.apiToken, requestJson);
        String wrappedRequest = gson.toJson(requestDto);
        session.getRemote().sendString(wrappedRequest);

        // enter a sleep loop to wait for the response
        int sleepRemainingMilliseconds = config.socketTimeout * 1000;
        while (!responseReceived && (sleepRemainingMilliseconds > 0)) {
            try {
                Thread.sleep(SLEEP_MILLISECONDS);
                sleepRemainingMilliseconds = sleepRemainingMilliseconds - SLEEP_MILLISECONDS;
            } catch (InterruptedException e) {
                throw new NeoHubException(String.format("Receive message timeout '%s'", e.getMessage()));
            }
        }

        NeohubWebSocketResponse wrappedResponse = gson.fromJson(responseJson, NeohubWebSocketResponse.class);
        return (wrappedResponse != null)
                && NeoHubBindingConstants.HM_SET_COMMAND_RESPONSE.equals(wrappedResponse.response)
                        ? wrappedResponse.response
                        : "";
    }

    @Override
    public void dispose() {
        closeSession();
        try {
            webSocketClient.stop();
        } catch (Exception e) {
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.trace("onConnect: ok");
        this.session = session;
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        logger.trace("onClose: code:{}, reason:{}", statusCode, reason);
        session = null;
    }

    @OnWebSocketError
    public void onError(Throwable cause) {
        logger.trace("onError: cause:{}", cause.getMessage());
        closeSession();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        responseReceived = true;
        logger.trace("onMessage: msg:{}", msg);
        responseJson = msg;
    }
}
