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
package org.openhab.binding.tado.internal.builder;

import static org.openhab.binding.tado.internal.api.TadoApiTypeUtils.*;

import java.io.IOException;
import java.util.List;

import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.api.ApiException;
import org.openhab.binding.tado.internal.api.model.ACFanLevel;
import org.openhab.binding.tado.internal.api.model.ACHorizontalSwing;
import org.openhab.binding.tado.internal.api.model.ACVerticalSwing;
import org.openhab.binding.tado.internal.api.model.AcMode;
import org.openhab.binding.tado.internal.api.model.AcModeCapabilities;
import org.openhab.binding.tado.internal.api.model.AirConditioningCapabilities;
import org.openhab.binding.tado.internal.api.model.CoolingZoneSetting;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.IntRange;
import org.openhab.binding.tado.internal.api.model.Power;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.TemperatureRange;

/**
 *
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class AirConditioningZoneSettingsBuilder extends ZoneSettingsBuilder {
    private static final AcMode DEFAULT_MODE = AcMode.COOL;
    private static final float DEFAULT_TEMPERATURE_C = 20.0f;
    private static final float DEFAULT_TEMPERATURE_F = 68.0f;

    @Override
    public GenericZoneSetting build(ZoneStateProvider zoneStateProvider, GenericZoneCapabilities genericCapabilities)
            throws IOException, ApiException {
        if (mode == HvacMode.OFF) {
            return coolingSetting(false);
        }

        CoolingZoneSetting setting = coolingSetting(true);
        setting.setMode(getAcMode(mode));
        if (temperature != null) {
            setting.setTemperature(temperature(temperature, temperatureUnit));
        }

        if (horizontalSwing != null) {
            setting.setHorizontalSwing(getHorizontalSwing(horizontalSwing));
        }

        if (verticalSwing != null) {
            setting.setVerticalSwing(getVerticalSwing(verticalSwing));
        }

        if (fanLevel != null) {
            setting.setFanLevel(getFanLevel(fanLevel));
        }

        addMissingSettingParts(zoneStateProvider, genericCapabilities, setting);

        return setting;
    }

    private void addMissingSettingParts(ZoneStateProvider zoneStateProvider,
            GenericZoneCapabilities genericCapabilities, CoolingZoneSetting setting) throws IOException, ApiException {
        if (setting.getMode() == null) {
            AcMode targetMode = getCurrentOrDefaultAcMode(zoneStateProvider);
            setting.setMode(targetMode);
        }

        AcModeCapabilities capabilities = getModeCapabilities((AirConditioningCapabilities) genericCapabilities,
                setting.getMode());

        if (capabilities.getTemperatures() != null && setting.getTemperature() == null) {
            TemperatureObject targetTemperature = getCurrentOrDefaultTemperature(zoneStateProvider,
                    capabilities.getTemperatures());
            setting.setTemperature(targetTemperature);
        }

        if (capabilities.getFanLevel() != null && !capabilities.getFanLevel().isEmpty()
                && setting.getFanLevel() == null) {
            ACFanLevel fanLevel = getCurrentOrDefaultFanLevel(zoneStateProvider, capabilities.getFanLevel());
            setting.setFanLevel(fanLevel);
        }

        if (capabilities.getHorizontalSwing() != null && !capabilities.getHorizontalSwing().isEmpty()
                && setting.getHorizontalSwing() == null) {
            ACHorizontalSwing horizontalSwing = getCurrentOrDefaultHorizontalSwing(zoneStateProvider,
                    capabilities.getHorizontalSwing());
            setting.setHorizontalSwing(horizontalSwing);
        }

        if (capabilities.getVerticalSwing() != null && !capabilities.getVerticalSwing().isEmpty()
                && setting.getVerticalSwing() == null) {
            ACVerticalSwing verticalSwing = getCurrentOrDefaultVerticalSwing(zoneStateProvider,
                    capabilities.getVerticalSwing());
            setting.setVerticalSwing(verticalSwing);
        }
    }

    private AcMode getCurrentOrDefaultAcMode(ZoneStateProvider zoneStateProvider) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        return zoneSetting.getMode() != null ? zoneSetting.getMode() : DEFAULT_MODE;
    }

    private TemperatureObject getCurrentOrDefaultTemperature(ZoneStateProvider zoneStateProvider,
            TemperatureRange temperatureRanges) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        Float defaultTemperature = temperatureUnit == TemperatureUnit.FAHRENHEIT ? DEFAULT_TEMPERATURE_F
                : DEFAULT_TEMPERATURE_C;
        Float temperature = (zoneSetting != null && zoneSetting.getTemperature() != null)
                ? getTemperatureInUnit(zoneSetting.getTemperature(), temperatureUnit)
                : defaultTemperature;
        IntRange temperatureRange = temperatureUnit == TemperatureUnit.FAHRENHEIT ? temperatureRanges.getFahrenheit()
                : temperatureRanges.getCelsius();

        Float finalTemperature = temperatureRange.getMax() >= temperature && temperatureRange.getMin() <= temperature
                ? temperature
                : temperatureRange.getMax();

        return temperature(finalTemperature, temperatureUnit);
    }

    private ACFanLevel getCurrentOrDefaultFanLevel(ZoneStateProvider zoneStateProvider, List<ACFanLevel> fanLevels)
            throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getFanLevel() != null && fanLevels.contains(zoneSetting.getFanLevel())) {
            return zoneSetting.getFanLevel();
        }

        return fanLevels.get(0);
    }

    private ACHorizontalSwing getCurrentOrDefaultHorizontalSwing(ZoneStateProvider zoneStateProvider,
            List<ACHorizontalSwing> horizontalSwings) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getHorizontalSwing() != null && horizontalSwings.contains(zoneSetting.getHorizontalSwing())) {
            return zoneSetting.getHorizontalSwing();
        }

        return horizontalSwings.get(0);
    }

    private ACVerticalSwing getCurrentOrDefaultVerticalSwing(ZoneStateProvider zoneStateProvider,
            List<ACVerticalSwing> verticalSwings) throws IOException, ApiException {
        CoolingZoneSetting zoneSetting = (CoolingZoneSetting) zoneStateProvider.getZoneState().getSetting();

        if (zoneSetting.getVerticalSwing() != null && verticalSwings.contains(zoneSetting.getVerticalSwing())) {
            return zoneSetting.getVerticalSwing();
        }

        return verticalSwings.get(0);
    }

    private CoolingZoneSetting coolingSetting(boolean powerOn) {
        CoolingZoneSetting setting = new CoolingZoneSetting();
        setting.setType(TadoSystemType.AIR_CONDITIONING);
        setting.setPower(powerOn ? Power.ON : Power.OFF);
        return setting;
    }
}
