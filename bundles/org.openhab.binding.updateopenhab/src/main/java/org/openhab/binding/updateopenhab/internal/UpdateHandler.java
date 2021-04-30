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

import static org.openhab.binding.updateopenhab.internal.BindingConstants.CHANNEL_UPDATE_COMMAND;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.updateopenhab.scripts.DebianUpdateExecutor;
import org.openhab.binding.updateopenhab.scripts.MacUpdateExecutor;
import org.openhab.binding.updateopenhab.scripts.WindowsUpdateExecutor;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpdateHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class UpdateHandler extends BaseThingHandler {

    protected final Logger logger = LoggerFactory.getLogger(UpdateHandler.class);

    private TargetVersion targetVersion;

    /**
     * Constructor
     */
    public UpdateHandler(Thing thing) {
        super(thing);
        targetVersion = TargetVersion.STABLE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_UPDATE_COMMAND.equals(channelUID.getId())) {
            updateState(channelUID, OnOffType.OFF);
            if (command instanceof RefreshType) {
                return;
            }
            if (OnOffType.ON.equals(command)) {
                switch (OperatingSystem.getOperatingSystemVersion()) {
                    case MAC:
                        scheduler.submit(new MacUpdateExecutor(targetVersion));
                        break;
                    case UNIX:
                        scheduler.submit(new DebianUpdateExecutor(targetVersion));
                        break;
                    case WINDOWS:
                        scheduler.submit(new WindowsUpdateExecutor(targetVersion));
                        break;
                    default:
                        logger.warn("OpenHAB updating not supported on {}",
                                OperatingSystem.getOperatingSystemVersion());
                }
            }
        }
    }

    @Override
    public void initialize() {
        Configuration config = getConfigAs(Configuration.class);
        try {
            targetVersion = TargetVersion.valueOf(config.targetVersion);
        } catch (IllegalArgumentException | NullPointerException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        OperatingSystem os = OperatingSystem.getOperatingSystemVersion();
        updateProperty(BindingConstants.PROPERTY_OPERATING_SYSTEM, os.toString());
        if (os == OperatingSystem.UNKNOWN) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
