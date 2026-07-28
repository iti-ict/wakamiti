/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public abstract class BaseModel implements Serializable {

    @Override
    public boolean equals(
            Object obj
    ) {
        return obj != null
                && (this.getClass().isAssignableFrom(obj.getClass()) || obj.getClass().isAssignableFrom(this.getClass()))
                && this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashValues());
    }

    protected abstract Object[] hashValues();

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        builder.setExcludeNullValues(true);
        builder.setExcludeFieldNames("metadata");
        return builder.build();
    }

}
