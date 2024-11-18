/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


public class Attachment {

    public Type attachmentType;
    public String comment;
    public String fileName;
    public String stream;

    public Attachment attachmentType(Type attachmentType) {
        this.attachmentType = attachmentType;
        return this;
    }

    public Type attachmentType() {
        return attachmentType;
    }

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

    public enum Type {
        GeneralAttachment,
        AfnStrip,
        BugFilingData,
        CodeCoverage,
        IntermediateCollectorData,
        RunConfig,
        TestImpactDetails,
        TmiTestRunDeploymentFiles,
        TmiTestRunReverseDeploymentFiles,
        TmiTestResultDetail,
        TmiTestRunSummary;
    }
}
