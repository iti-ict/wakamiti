/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


public class OrderElement {

    enum Type {
        ASC, DESC
    }

    private Field field;
    private Type type;

    private OrderElement(Field field, Type type) {
        this.field = field;
        this.type = type;
    }

    public static OrderElement asc(Field field) {
        return new OrderElement(field, Type.ASC);
    }

    public static OrderElement desc(Field field) {
        return new OrderElement(field, Type.DESC);
    }

    public static OrderElement of(Field field) {
        return new OrderElement(field, null);
    }

    public static OrderElement asc(String field) {
        return asc(Field.of(field));
    }

    public static OrderElement desc(String field) {
        return desc(Field.of(field));
    }

    public static OrderElement of(String field) {
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
