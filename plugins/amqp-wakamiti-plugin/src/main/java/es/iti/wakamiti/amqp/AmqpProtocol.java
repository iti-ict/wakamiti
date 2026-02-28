/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.amqp;


import java.util.Map;


/**
 * Supported AMQP wire protocols in this plugin.
 */
public enum AmqpProtocol {

    AMQP_1_0,
    AMQP_0_9_1;

    private static final Map<String, AmqpProtocol> ALIASES = Map.ofEntries(
        Map.entry("1_0", AMQP_1_0),
        Map.entry("AMQP10", AMQP_1_0),
        Map.entry("0_9_1", AMQP_0_9_1),
        Map.entry("0_9", AMQP_0_9_1),
        Map.entry("0_8", AMQP_0_9_1),
        Map.entry("AMQP091", AMQP_0_9_1),
        Map.entry("AMQP09", AMQP_0_9_1),
        Map.entry("AMQP08", AMQP_0_9_1)
    );

    /**
     * Parses a protocol string with a permissive alias strategy.
     * <p>
     * Accepted examples:
     * <ul>
     *   <li>{@code AMQP_1_0}, {@code amqp-1.0}, {@code 1_0}, {@code amqp10}</li>
     *   <li>{@code AMQP_0_9_1}, {@code amqp-0.9.1}, {@code 0_9_1}, {@code amqp091}</li>
     * </ul>
     *
     * @param value raw string value from config or steps
     * @param defaultValue fallback when value is {@code null} or blank
     * @return parsed protocol
     * @throws IllegalArgumentException if value is not blank and cannot be resolved
     */
    public static AmqpProtocol parseOrDefault(
            String value,
            AmqpProtocol defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim()
            .replace('-', '_')
            .replace('.', '_')
            .toUpperCase();
        AmqpProtocol protocol = ALIASES.get(normalized);
        return protocol != null ? protocol : AmqpProtocol.valueOf(normalized);
    }
}
