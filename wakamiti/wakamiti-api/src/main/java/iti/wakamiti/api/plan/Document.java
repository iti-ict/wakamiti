/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.wakamiti.api.plan;


import java.util.function.UnaryOperator;


public class Document implements PlanNodeData {

    private final String content;
    private final String contentType;


    public Document(String content) {
        this.content = content;
        this.contentType = null;
    }


    public Document(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }


    public String getContent() {
        return content;
    }


    public String getContentType() {
        return contentType;
    }


    @Override
    public PlanNodeData copy() {
        return new Document(content, contentType);
    }


    @Override
    public PlanNodeData copyReplacingVariables(UnaryOperator<String> replacer) {
        return new Document(replacer.apply(content), contentType);
    }

}