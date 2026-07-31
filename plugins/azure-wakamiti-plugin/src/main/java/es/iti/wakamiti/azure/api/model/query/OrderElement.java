/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


/**
 * Provides the Order Element functionality used by Wakamiti.
 */
public final class OrderElement {

    /**
     * Defines the values supported by Type.
     */
    enum Type {

        /** Sorts query results from the lowest field value to the highest. */
        ASC,
        /** Sorts query results from the highest field value to the lowest. */
        DESC

    }

    private Field field;
    private Type type;

    private OrderElement(
            Field field,
            Type type
    ) {
        this.field = field;
        this.type = type;
    }

    /**
     * Creates an ascending WIQL sort expression for a normalized field.
     *
     * @param field field to sort
     * @return ascending WIQL order element
     */
    public static OrderElement asc(
            Field field
    ) {
        return new OrderElement(field, Type.ASC);
    }

    /**
     * Creates a descending WIQL sort expression for a normalized field.
     *
     * @param field field to sort
     * @return descending WIQL order element
     */
    public static OrderElement desc(
            Field field
    ) {
        return new OrderElement(field, Type.DESC);
    }

    /**
     * Creates a WIQL sort expression without forcing a direction.
     *
     * @param field field to sort
     * @return order element using Azure's default direction
     */
    public static OrderElement of(
            Field field
    ) {
        return new OrderElement(field, null);
    }

    /**
     * Creates an ascending WIQL sort expression from an Azure field name.
     *
     * @param field Azure field name
     * @return ascending WIQL order element
     */
    public static OrderElement asc(
            String field
    ) {
        return asc(Field.of(field));
    }

    /**
     * Creates a descending WIQL sort expression from an Azure field name.
     *
     * @param field Azure field name
     * @return descending WIQL order element
     */
    public static OrderElement desc(
            String field
    ) {
        return desc(Field.of(field));
    }

    /**
     * Creates a direction-neutral WIQL sort expression from an Azure field name.
     *
     * @param field Azure field name
     * @return order element using Azure's default direction
     */
    public static OrderElement of(
            String field
    ) {
        return of(Field.of(field));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(field.toString());
        if (type != null) {
            builder.append(" ").append(type);
        }
        return builder.toString();
    }

}
