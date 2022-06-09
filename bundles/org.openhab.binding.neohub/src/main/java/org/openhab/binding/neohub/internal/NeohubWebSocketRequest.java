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

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for web socket communications (requests).
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
public class NeohubWebSocketRequest {
    public String message_type;
    public Message message;

    public void setFields(String token, String command) {
        message_type = NeoHubBindingConstants.HM_GET_COMMAND_QUEUE;
        message = new Message();
        message.token = token;
        message.COMMANDS = new ArrayList<>();
        message.COMMANDS.add(new Command());
        message.COMMANDS.get(0).COMMANDID = 1;
        message.COMMANDS.get(0).COMMAND = command;
    }

    public static class Message {
        String token;
        List<Command> COMMANDS;
    }

    public static class Command {
        String COMMAND;
        int COMMANDID;
    }
}
