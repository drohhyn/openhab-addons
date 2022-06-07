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
package org.openhab.binding.hdpowerview.internal.database;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class containing the database of all known shade 'types' and their respective 'capabilities'.
 *
 * If user systems detect shade types that are not in the database, then this class can issue logger warning messages
 * indicating such absence, and prompting the user to report it to developers so that the database and the respective
 * binding functionality can (hopefully) be extended over time.
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
public class ShadeCapabilitiesDatabase {

    private final Logger logger = LoggerFactory.getLogger(ShadeCapabilitiesDatabase.class);

    // Capability 9 is not tiltAnywhere - it requires the rear shade to be open and the front to be closed

    /*
     * Database of known shade capabilities.
     */
    private static final Map<Integer, Capabilities> CAPABILITIES_DATABASE = Arrays.asList(
    // @formatter:off
            new Capabilities(0).primary()                                                       .text("Bottom Up"),
            new Capabilities(1).primary()        .tiltOnClosed()                                .text("Bottom Up Tilt 90°"),
            new Capabilities(2).primary()        .tiltAnywhere().tilt180()                      .text("Bottom Up Tilt 180°"),
            new Capabilities(3).primary()        .tiltAnywhere().tilt180()                      .text("Vertical Tilt 180°"),
            new Capabilities(4).primary()                                                       .text("Vertical"),
            new Capabilities(5)                  .tiltAnywhere().tilt180()                      .text("Tilt Only 180°"),
            new Capabilities(6).primaryInverted()                                               .text("Top Down"),
            new Capabilities(7).primary()                                 .secondary()          .text("Top Down Bottom Up"),
            new Capabilities(8).primary()                                 .secondaryOverlapped().text("Dual Overlapped"),
            new Capabilities(9).primary()        .tiltOnClosed()          .secondaryOverlapped().text("Dual Overlapped Tilt 90°")
                               .comment("The 'tiltOnClosed()' applies to the primary shade"),
    // @formatter:on
            new Capabilities()).stream().collect(Collectors.toMap(Capabilities::getValue, Function.identity()));

    /*
     * Database of known shade types and corresponding capabilities.
     */
    private static final Map<Integer, Type> TYPE_DATABASE = Arrays.asList(
    // @formatter:off
            new Type( 1).capabilities(0).text("Generic"),
            new Type( 4).capabilities(0).text("Roman"),
            new Type( 5).capabilities(0).text("Bottom Up"),
            new Type( 6).capabilities(0).text("Duette"),
            new Type( 7).capabilities(6).text("Top Down"),
            new Type( 8).capabilities(7).text("Duette Top Down Bottom Up"),
            new Type( 9).capabilities(7).text("Duette DuoLite Top Down Bottom Up"),
            new Type(18).capabilities(1).text("Pirouette"),
            new Type(23).capabilities(1).text("Silhouette"),
            new Type(38).capabilities(9).text("Silhouette Duolite"),
            new Type(42).capabilities(0).text("M25T Roller Blind"),
            new Type(43).capabilities(1).text("Facette"),
            new Type(44).capabilities(0).text("Twist")
                        .comment("Shade has functionality of a capabilities 1 shade").typeCapabilities(1),
            new Type(47).capabilities(7).text("Pleated Top Down Bottom Up"),
            new Type(49).capabilities(0).text("AC Roller"),
            new Type(51).capabilities(2).text("Venetian"),
            new Type(54).capabilities(3).text("Vertical Slats Left Stack"),
            new Type(55).capabilities(3).text("Vertical Slats Right Stack"),
            new Type(56).capabilities(3).text("Vertical Slats Split Stack"),
            new Type(62).capabilities(2).text("Venetian"),
            new Type(65).capabilities(8).text("Vignette Duolite"),
            new Type(66).capabilities(5).text("Shutter"),
            new Type(69).capabilities(4).text("Curtain Left Stack"),
            new Type(70).capabilities(4).text("Curtain Right Stack"),
            new Type(71).capabilities(4).text("Curtain Split Stack"),
            new Type(79).capabilities(8).text("Duolite Lift"),
    // @formatter:on
            new Type()).stream().collect(Collectors.toMap(Type::getValue, Function.identity()));

    /**
     * Base class that is extended by Type and Capabilities classes.
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    private static class Base<T extends Base<T>> {
        protected int intValue = -1;
        protected String text = "-- not in database --";
        protected String comment = "";

        @SuppressWarnings("unchecked")
        protected T comment(String comment) {
            this.comment = comment;
            return (T) this;
        }

        public Integer getValue() {
            return intValue;
        }

        public String getComment() {
            return comment;
        }

        @Override
        public String toString() {
            return String.format("%s (%d)", text, intValue);
        }
    }

    /**
     * Describes a shade type entry in the database; implements 'capabilities' parameter.
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    public static class Type extends Base<Type> {
        private int capabilities = -1;
        private int typeCapabilities = -1;

        protected Type() {
        }

        protected Type(int type) {
            intValue = type;
        }

        protected Type text(String text) {
            this.text = text;
            return this;
        }

        protected Type capabilities(int capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        protected Type typeCapabilities(int capabilities) {
            this.typeCapabilities = capabilities;
            return this;
        }

        /**
         * Get shade types's 'capabilities'.
         *
         * @return 'capabilities'.
         */
        public int getCapabilities() {
            return capabilities;
        }

        /**
         * Get shade's type specific 'capabilities'.
         *
         * @return 'typeCapabilities'.
         */
        public int getTypeCapabilities() {
            return typeCapabilities;
        }
    }

    /**
     * Describes a shade 'capabilities' entry in the database; adds properties indicating its supported functionality.
     *
     * @author Andrew Fiddian-Green - Initial Contribution
     */
    public static class Capabilities extends Base<Capabilities> {
        private boolean supportsPrimary;
        private boolean supportsSecondary;
        private boolean supportsTiltOnClosed;
        private boolean supportsTiltAnywhere;
        private boolean supportsSecondaryOverlapped;
        private boolean primaryInverted;
        private boolean tilt180Degrees;

        public Capabilities() {
        }

        protected Capabilities secondaryOverlapped() {
            supportsSecondaryOverlapped = true;
            return this;
        }

        protected Capabilities(int capabilities) {
            intValue = capabilities;
        }

        protected Capabilities text(String text) {
            this.text = text;
            return this;
        }

        protected Capabilities primary() {
            supportsPrimary = true;
            return this;
        }

        protected Capabilities tiltOnClosed() {
            supportsTiltOnClosed = true;
            return this;
        }

        protected Capabilities secondary() {
            supportsSecondary = true;
            return this;
        }

        protected Capabilities tiltAnywhere() {
            supportsTiltAnywhere = true;
            return this;
        }

        protected Capabilities primaryInverted() {
            supportsPrimary = true;
            primaryInverted = true;
            return this;
        }

        protected Capabilities tilt180() {
            tilt180Degrees = true;
            return this;
        }

        /**
         * Check if the Capabilities class instance supports a primary shade.
         *
         * @return true if it supports a primary shade.
         */
        public boolean supportsPrimary() {
            return supportsPrimary;
        }

        /**
         * Check if the Capabilities class instance supports a vane/tilt function (by means of a second motor).
         *
         * @return true if it supports a vane/tilt function (by means of a second motor).
         */
        public boolean supportsTiltAnywhere() {
            return supportsTiltAnywhere;
        }

        /**
         * Check if the Capabilities class instance supports a secondary shade.
         *
         * @return true if it supports a secondary shade.
         */
        public boolean supportsSecondary() {
            return supportsSecondary;
        }

        /**
         * Check if the Capabilities class instance if the primary shade is inverted.
         *
         * @return true if the primary shade is inverted.
         */
        public boolean isPrimaryInverted() {
            return primaryInverted;
        }

        /**
         * Check if the Capabilities class instance supports 'tilt on closed'.
         *
         * Note: Simple bottom up or vertical shades that do not have independent vane controls, can be tilted in a
         * simple way, only when they are fully closed, by moving the shade motor a bit further.
         *
         * @return true if the it supports tilt on closed.
         */
        public boolean supportsTiltOnClosed() {
            return supportsTiltOnClosed && !supportsTiltAnywhere;
        }

        /**
         * Check if the Capabilities class instance supports 180 degrees tilt.
         *
         * @return true if the tilt range is 180 degrees.
         */
        public boolean supportsTilt180() {
            return tilt180Degrees;
        }

        /**
         * Check if the Capabilities class instance supports an overlapped secondary shade.
         * e.g. a 'DuoLite' or blackout shade.
         *
         * @return true if the shade supports a secondary overlapped shade.
         */
        public boolean supportsSecondaryOverlapped() {
            return supportsSecondaryOverlapped;
        }
    }

    /**
     * Determines if a given shade 'type' is in the database.
     *
     * @param type the shade 'type' parameter.
     * @return true if the shade 'type' is known.
     */
    public boolean isTypeInDatabase(int type) {
        return TYPE_DATABASE.containsKey(type);
    }

    /**
     * Determines if a given 'capabilities' value is in the database.
     *
     * @param capabilities the shade 'capabilities' parameter
     * @return true if the 'capabilities' value is known
     */
    public boolean isCapabilitiesInDatabase(int capabilities) {
        return CAPABILITIES_DATABASE.containsKey(capabilities);
    }

    /**
     * Return a Type class instance that corresponds to the given 'type' parameter.
     *
     * @param type the shade 'type' parameter.
     * @return corresponding instance of Type class.
     */
    public Type getType(int type) {
        return TYPE_DATABASE.getOrDefault(type, new Type());
    }

    /**
     * Return a Capabilities class instance that corresponds to the given 'capabilities' parameter.
     *
     * @param capabilities the shade 'capabilities' parameter.
     * @return corresponding instance of Capabilities class.
     */
    public Capabilities getCapabilities(int capabilities) {
        return CAPABILITIES_DATABASE.getOrDefault(capabilities, new Capabilities());
    }

    /**
     * Return a Capabilities class instance that corresponds to the given 'type' parameter.
     *
     * @param type the shade type.
     * @return corresponding instance of Capabilities class.
     */
    public Capabilities getCapabilitiesForType(int type) {
        return getCapabilities(TYPE_DATABASE.getOrDefault(type, new Type()).getTypeCapabilities());
    }

    private static final String REQUEST_DEVELOPERS_TO_UPDATE = " => Please request developers to update the database!";

    /**
     * Log a message indicating that 'type' is not in database.
     *
     * @param type
     */
    public void logTypeNotInDatabase(int type) {
        logger.warn("The shade 'type:{}' is not in the database!{}", type, REQUEST_DEVELOPERS_TO_UPDATE);
    }

    /**
     * Log a message indicating that 'capabilities' is not in database.
     *
     * @param capabilities
     */
    public void logCapabilitiesNotInDatabase(int type, int capabilities) {
        logger.warn("The 'capabilities:{}' for shade 'type:{}' are not in the database!{}", capabilities, type,
                REQUEST_DEVELOPERS_TO_UPDATE);
    }

    /**
     * Log a message indicating the type's capabilities and the passed capabilities are not equal.
     *
     * @param type
     * @param capabilities
     */
    public void logCapabilitiesMismatch(int type, int capabilities) {
        logger.warn("The 'capabilities:{}' reported by shade 'type:{}' don't match the database!{}", capabilities, type,
                REQUEST_DEVELOPERS_TO_UPDATE);
    }

    /**
     * Log a message indicating that a shade's secondary/vanes support, as observed via its actual JSON payload, does
     * not match the expected value as declared in its 'type' and 'capabilities'.
     *
     * @param propertyKey
     * @param type
     * @param capabilities
     * @param propertyValue
     */
    public void logPropertyMismatch(String propertyKey, int type, int capabilities, boolean propertyValue) {
        logger.warn(
                "The '{}:{}' property actually reported by shade 'type:{}' is different "
                        + "than expected from its 'capabilities:{}' in the database!{}",
                propertyKey, propertyValue, type, capabilities, REQUEST_DEVELOPERS_TO_UPDATE);
    }
}
