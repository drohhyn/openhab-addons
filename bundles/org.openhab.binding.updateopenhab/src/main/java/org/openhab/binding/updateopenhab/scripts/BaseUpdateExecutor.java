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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Executors;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.updateopenhab.internal.TargetVersion;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseUpdateExecutor} is the shell script for updating OpenHab on this OS
 *
 * @author AndrewFG
 */
@NonNullByDefault
public abstract class BaseUpdateExecutor {

    protected final Logger logger = LoggerFactory.getLogger(BaseUpdateExecutor.class);

    private static final String RELEASE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-release-local/org/openhab/distro/openhab/maven-metadata.xml";
    private static final String MILESTONE_ARTIFACT_URL = "https://openhab.jfrog.io/artifactory/libs-milestone-local/org/openhab/distro/openhab/maven-metadata.xml";

    private static final String UPDATE_SCRIPT_FILENAME = "openhab-update";

    protected TargetVersion targetVersion;
    protected String runDirectory;
    protected String fileExtension;

    /**
     * Private class to read and process the standard output of the shell command
     *
     * @author Andrew Fiddian-Green - Initial contribution
     */
    protected static class InputStreamEater implements Runnable {

        private InputStream inputStream;
        private Logger logger;

        public InputStreamEater(InputStream inputStream, Logger logger) {
            this.inputStream = inputStream;
            this.logger = logger;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach((line) -> {
                logger.trace(line);
            });
        }
    }

    public BaseUpdateExecutor(TargetVersion targetVersion) {
        this.targetVersion = targetVersion;
        runDirectory = "";
        fileExtension = "";
    }

    protected String getScriptFileName() {
        return runDirectory + File.separator + UPDATE_SCRIPT_FILENAME + fileExtension;
    }

    protected boolean writeFile(String filename, String contents) {
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(filename));
            writer.write(contents);
            writer.close();
            logger.debug("Success writing file {}", filename);
            return true;
        } catch (IOException e1) {
            logger.warn("Failure writing file {}", filename);
        }
        return false;
    }

    protected void runProcess(ProcessBuilder builder) {
        try {
            logger.info("Starting update of OpenHAB to latest {} version", targetVersion.label);
            Process process = builder.start();
            InputStreamEater inputStreamEater = new InputStreamEater(process.getInputStream(), logger);
            Executors.newSingleThreadExecutor().submit(inputStreamEater);
            int exitcode = process.waitFor();
            if (exitcode == 0) {
                logger.info("OpenHAB was updated to v{} ", getNextVersionString());
            } else {
                logger.warn("Update of OpenHAB to failed with exit code '{}'", exitcode);
            }
        } catch (IOException e) {
            logger.warn("Update of OpenHAB to failed with error '{}'", e.getMessage());
        } catch (InterruptedException e) {
            logger.warn("Update of OpenHAB was interruped");
        }
    }

    protected String getOpenHabRootFolder() {
        return OpenHAB.getConfigFolder().replace("conf", "");
    }

    private String getLatestVersionFrom(String url) {
        String version = "";
        XMLStreamReader reader;
        try {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(new URL(url).openStream());
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    if ("latest".equals(reader.getLocalName())) {
                        version = reader.getElementText();
                        break;
                    }
                }
            }
            reader.close();
        } catch (IOException | XMLStreamException e) {
        }
        return version;
    }

    protected String getNextVersionString() {
        switch (targetVersion) {
            case SNAPSHOT:
                String[] version = OpenHAB.getVersion().split("-");
                return version[0] + "-SNAPSHOT";
            case MILESTONE:
                return getLatestVersionFrom(MILESTONE_ARTIFACT_URL);
            case STABLE:
                return getLatestVersionFrom(RELEASE_ARTIFACT_URL);
        }
        return "";
    }
}
