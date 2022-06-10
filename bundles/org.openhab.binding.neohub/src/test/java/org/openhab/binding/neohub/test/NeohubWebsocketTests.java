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
package org.openhab.binding.neohub.test;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.neohub.internal.NeoHubBindingConstants;
import org.openhab.binding.neohub.internal.NeoHubConfiguration;
import org.openhab.binding.neohub.internal.NeoHubWebSocket;

/**
 * JUnit for testing web sockets.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NeohubWebsocketTests {

    @Test
    void testWebSocket() {
        // create the web socket class
        NeoHubConfiguration config = new NeoHubConfiguration();
        config.hostName = "192.168.1.109";
        config.portNumber = NeoHubBindingConstants.PORT_WSS;
        config.socketTimeout = 5;
        config.apiToken = "3fee0f8d-53f6-4b64-a929-1a92ee745304";
        // send the request, and log the response
        try {
            NeoHubWebSocket socket = new NeoHubWebSocket(config);
            String requestJson = NeoHubBindingConstants.CMD_CODE_FIRMWARE;
            System.out.println(requestJson);
            String responseJson = socket.sendMessage(requestJson);
            System.out.println(responseJson);
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }
}
