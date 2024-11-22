/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class Attachment {

    @JsonProperty
    private final String attachmentType = "GeneralAttachment";
    @JsonProperty
    private String comment;
    @JsonProperty
    private String fileName;
    @JsonProperty
    private String stream;

    public Attachment comment(String comment) {
        this.comment = comment;
        return this;
    }

    public String comment() {
        return comment;
    }

    public Attachment fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String fileName() {
        return fileName;
    }

    public Attachment stream(String stream) {
        this.stream = stream;
        return this;
    }

    public String stream() {
        return stream;
    }
}
