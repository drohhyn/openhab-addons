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
package org.openhab.binding.updateopenhab.scripts;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.updateopenhab.internal.TargetVersion;

/**
 * The {@link DebianUpdateExecutor} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG
 */
@NonNullByDefault
public class DebianUpdateExecutor extends BaseUpdateExecutor implements Runnable {

    private static final String COMMAND_ID = "sh";
    private static final String COMMAND_SWITCH = "-c";
    private static final String FILE_EXTENSION = ".sh";
    private static final String APT_FILENAME = "/etc/apt/sources.list.d/openhab.list";
    private static final String DOWNLOAD_SOURCE_FMT = "deb https://openhab.jfrog.io/artifactory/openhab-linuxpkg %s main";
    private static final String SCRIPT_COMMAND = "apt-get update\n";

    public DebianUpdateExecutor(TargetVersion targetVersion) {
        super(targetVersion);
        String userHome = System.getProperty("user.home");
        runDirectory = userHome != null ? userHome : "";
        fileExtension = FILE_EXTENSION;
    }

    /**
     * Method to execute the OpenHAB update script
     */
    @Override
    public void run() {
        if (!writeFile(APT_FILENAME, String.format(DOWNLOAD_SOURCE_FMT, targetVersion.label))) {
            return;
        }
        if (!writeFile(getScriptFileName(), SCRIPT_COMMAND)) {
            return;
        }

        ProcessBuilder builder = new ProcessBuilder();
        builder.command().add(COMMAND_ID);
        builder.command().add(COMMAND_SWITCH);
        builder.directory(new File(runDirectory));
        builder.command().add(getScriptFileName());

        runProcess(builder);
    }
}
