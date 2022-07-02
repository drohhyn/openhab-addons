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
package org.openhab.binding.velux.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link IVeluxActions} defines rule action interface for rebooting the bridge
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public interface IVeluxActions {

    /**
     * Action to send a reboot command to a Velux Bridge
     *
     * @return true if the command was sent
     * @throws IllegalStateException if something is wrong
     */
    Boolean rebootBridge() throws IllegalStateException;

    /**
     * Action to send a relative move command to a Velux actuator
     *
     * @param nodeId the node Id in the bridge
     * @param relativePercent the target position relative to its current position (-100% <= relativePercent <= +100%)
     * @return true if the command was sent
     * @throws NumberFormatException if either of the arguments is not an integer, or out of range
     * @throws IllegalStateException if anything else is wrong
     */
    Boolean moveRelative(String nodeId, String relativePercent) throws NumberFormatException, IllegalStateException;

    /**
     * Action to simultaneously set the shade main position and the vane position.
     *
     * @param bridgeIndex the node Id in the bridge (valid node in range 1..200)
     * @param mainPercent the desired main position (range 0..100)
     * @param vanePercent the desired vane position (range 0..100)
     * @return true if the command was sent
     * @throws NumberFormatException if any of the arguments are not an integer
     * @throws IllegalArgumentException if any of the arguments are invalid
     * @throws IllegalStateException if anything else is wrong
     */
    Boolean setMainAndVanePosition(Integer bridgeIndex, Integer mainPercent, Integer vanePercent)
            throws NumberFormatException, IllegalArgumentException, IllegalStateException;
}
