/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Attachment functionality used by Wakamiti.
 */
public class Attachment {

    @JsonProperty
    private final String attachmentType = "GeneralAttachment";
    @JsonProperty
    private String comment;
    @JsonProperty
    private String fileName;
    @JsonProperty
    private String stream;

    /**
     * Sets the explanatory text displayed alongside the run attachment.
     *
     * @param comment description shown with the Azure test-run attachment
     * @return this attachment
     */
    public Attachment comment(
            String comment
    ) {
        this.comment = comment;
        return this;
    }

    /**
     * @return description shown to Azure DevOps users
     */
    public String comment() {
        return comment;
    }

    /**
     * Sets the attachment filename that Azure presents to users.
     *
     * @param fileName attachment name including its extension
     * @return this attachment
     */
    public Attachment fileName(
            String fileName
    ) {
        this.fileName = fileName;
        return this;
    }

    /**
     * @return attachment name presented by Azure DevOps
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Sets the Base64 payload sent in the Azure attachment request.
     *
     * @param stream Base64-encoded binary attachment content
     * @return this attachment
     */
    public Attachment stream(
            String stream
    ) {
        this.stream = stream;
        return this;
    }

    /**
     * @return Base64-encoded content sent to Azure DevOps
     */
    public String stream() {
        return stream;
    }

}
