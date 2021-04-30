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
package org.openhab.binding.updateopenhab.test;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.updateopenhab.internal.OperatingSystem;
import org.openhab.binding.updateopenhab.internal.TargetVersion;
import org.openhab.binding.updateopenhab.scripts.DebianUpdateExecutor;
import org.openhab.binding.updateopenhab.scripts.MacUpdateExecutor;
import org.openhab.binding.updateopenhab.scripts.WindowsUpdateExecutor;

/**
 * The {@link RunScript} is a JUnit test for running the scripts
 *
 * @author AndrewFG
 */
@NonNullByDefault
class RunScript {

    @Test
    void runScripts() {
        for (OperatingSystem os : OperatingSystem.values()) {
            for (TargetVersion targetVer : TargetVersion.values()) {
                runExecutor(os, targetVer);
            }
        }
    }

    private void runExecutor(OperatingSystem os, TargetVersion targetVer) {
        switch (os) {
            case MAC:
                new MacUpdateExecutor(targetVer).run();
                break;
            case UNIX:
                new DebianUpdateExecutor(targetVer).run();
                break;
            case WINDOWS:
                new WindowsUpdateExecutor(targetVer).run();
                break;
            default:
        }
    }
}
