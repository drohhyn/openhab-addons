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
package org.openhab.binding.tado.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tado.internal.TadoBindingConstants.FanSpeed;
import org.openhab.binding.tado.internal.TadoBindingConstants.HvacMode;
import org.openhab.binding.tado.internal.TadoBindingConstants.TemperatureUnit;
import org.openhab.binding.tado.internal.api.model.AcFanSpeed;
import org.openhab.binding.tado.internal.api.model.AcMode;
import org.openhab.binding.tado.internal.api.model.AcModeCapabilities;
import org.openhab.binding.tado.internal.api.model.AirConditioningCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.ManualTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionTemplate;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.TadoModeTerminationCondition;
import org.openhab.binding.tado.internal.api.model.TemperatureObject;
import org.openhab.binding.tado.internal.api.model.TimerTerminationCondition;
import org.openhab.binding.tado.internal.api.model.TimerTerminationConditionTemplate;

/**
 * Utility methods for the conversion of API types.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
@NonNullByDefault
public class TadoApiTypeUtils {

    public static OverlayTerminationCondition getTerminationCondition(OverlayTerminationConditionType type,
            int timerDurationInSeconds) {
        switch (type) {
            case TIMER:
                return timerTermination(timerDurationInSeconds);
            case MANUAL:
                return manualTermination();
            case TADO_MODE:
                return tadoModeTermination();
            default:
                throw new IllegalArgumentException("Unexpected OverlayTerminationConditionType " + type);
        }
    }

    public static OverlayTerminationCondition cleanTerminationCondition(
            OverlayTerminationCondition terminationCondition) {
        OverlayTerminationConditionType conditionType = terminationCondition.getType();

        int timerDuration = 0;
        if (conditionType == OverlayTerminationConditionType.TIMER) {
            Integer duration = ((TimerTerminationCondition) terminationCondition).getRemainingTimeInSeconds();
            if (duration != null) {
                timerDuration = duration.intValue();
            }
        }

        return getTerminationCondition(conditionType, timerDuration);
    }

    public static OverlayTerminationCondition terminationConditionTemplateToTerminationCondition(
            OverlayTerminationConditionTemplate template) {
        OverlayTerminationConditionType conditionType = template.getType();

        int timerDuration = 0;
        if (conditionType == OverlayTerminationConditionType.TIMER) {
            Integer duration = ((TimerTerminationConditionTemplate) template).getDurationInSeconds();
            if (duration != null) {
                timerDuration = duration.intValue();
            }
        }

        return getTerminationCondition(conditionType, timerDuration);
    }

    public static TimerTerminationCondition timerTermination(int durationInSeconds) {
        TimerTerminationCondition terminationCondition = new TimerTerminationCondition();
        terminationCondition.setType(OverlayTerminationConditionType.TIMER);
        terminationCondition.setDurationInSeconds(durationInSeconds);
        return terminationCondition;
    }

    public static ManualTerminationCondition manualTermination() {
        ManualTerminationCondition terminationCondition = new ManualTerminationCondition();
        terminationCondition.setType(OverlayTerminationConditionType.MANUAL);
        return terminationCondition;
    }

    public static TadoModeTerminationCondition tadoModeTermination() {
        TadoModeTerminationCondition terminationCondition = new TadoModeTerminationCondition();
        terminationCondition.setType(OverlayTerminationConditionType.TADO_MODE);
        return terminationCondition;
    }

    public static TemperatureObject temperature(float degree, TemperatureUnit temperatureUnit) {
        TemperatureObject temperature = new TemperatureObject();
        if (temperatureUnit == TemperatureUnit.FAHRENHEIT) {
            temperature.setFahrenheit(degree);
        } else {
            temperature.setCelsius(degree);
        }

        return temperature;
    }

    public static Float getTemperatureInUnit(TemperatureObject temperature, TemperatureUnit temperatureUnit) {
        return temperatureUnit == TemperatureUnit.FAHRENHEIT ? temperature.getFahrenheit() : temperature.getCelsius();
    }

    public static AcMode getAcMode(HvacMode mode) {
        switch (mode) {
            case HEAT:
                return AcMode.HEAT;
            case COOL:
                return AcMode.COOL;
            case FAN:
                return AcMode.FAN;
            case DRY:
                return AcMode.DRY;
            case AUTO:
                return AcMode.AUTO;
            default:
                throw new IllegalArgumentException("Unexpected AcMode " + mode);
        }
    }

    public static AcFanSpeed getAcFanSpeed(FanSpeed fanSpeed) {
        switch (fanSpeed) {
            case AUTO:
                return AcFanSpeed.AUTO;
            case HIGH:
                return AcFanSpeed.HIGH;
            case MIDDLE:
                return AcFanSpeed.MIDDLE;
            case LOW:
                return AcFanSpeed.LOW;
            default:
                throw new IllegalArgumentException("Unexpected AcFanSpeed " + fanSpeed);
        }
    }

    public static AcModeCapabilities getModeCapabilities(AcMode acMode,
            @Nullable GenericZoneCapabilities capabilities) {
        AirConditioningCapabilities acCapabilities;

        if (capabilities instanceof AirConditioningCapabilities) {
            acCapabilities = (AirConditioningCapabilities) capabilities;
        } else {
            acCapabilities = new AirConditioningCapabilities();
        }

        switch (acMode) {
            case COOL:
                return acCapabilities.getCOOL();
            case HEAT:
                return acCapabilities.getHEAT();
            case DRY:
                return acCapabilities.getDRY();
            case AUTO:
                return acCapabilities.getAUTO();
            case FAN:
                return acCapabilities.getFAN();
            default:
                throw new IllegalArgumentException("Unexpected AcMode " + acMode);
        }
    }
}
