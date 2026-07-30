/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.model;


import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Attachment functionality used by Wakamiti.
 */
public class Attachment {

    @JsonProperty
    private String file;

    /**
     * @param fileName local attachment path submitted to Jira
     * @return this attachment descriptor
     */
    public Attachment file(
            String fileName
    ) {
        this.file = fileName;
        return this;
    }

    /**
     * @return local attachment path submitted to Jira
     */
    public String file() {
        return file;
    }

}
