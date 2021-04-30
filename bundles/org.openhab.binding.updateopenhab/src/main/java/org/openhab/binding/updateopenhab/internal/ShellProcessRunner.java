/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.updateopenhab.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShellProcessRunner} is responsible for running an external shell process
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShellProcessRunner {

    private List<String> arguments = new ArrayList<>();

    public ShellProcessRunner(String command, String scriptFilePathName) {
        arguments.add(command);
        arguments.add(scriptFilePathName);
    }

    public void Execute() {
        ProcessBuilder builder = new ProcessBuilder(arguments);
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
    }
}
