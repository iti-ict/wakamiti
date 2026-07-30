/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


/**
 * Provides the Field functionality used by Wakamiti.
 */
public final class Field {

    /** Azure DevOps system field containing the numeric work-item identifier. */
    public static final String ID = "System.Id";
    /** Azure DevOps system field containing the work-item title. */
    public static final String TITLE = "System.Title";
    /** Azure DevOps system field containing the HTML description. */
    public static final String DESCRIPTION = "System.Description";
    /** Test Case field containing serialized manual test steps. */
    public static final String STEPS = "Microsoft.VSTS.TCM.Steps";
    /** Test Case field declaring shared parameter definitions. */
    public static final String PARAMETERS = "Microsoft.VSTS.TCM.Parameters";
    /** Test Case field containing the local parameter data source. */
    public static final String LOCAL_DATASOURCE = "Microsoft.VSTS.TCM.LocalDataSource";
    /** Semicolon-separated Azure DevOps work-item tags. */
    public static final String TAGS = "System.Tags";
    /** Classification path of the owning Azure DevOps area. */
    public static final String AREA_PATH = "System.AreaPath";
    /** Classification path of the owning Azure DevOps iteration. */
    public static final String ITERATION_PATH = "System.IterationPath";
    /** Current workflow state of a work item. */
    public static final String STATE = "System.State";
    /** General system type field used by Azure DevOps query responses. */
    public static final String TYPE = "System.Type";
    /** Work-item type name, for example {@code Test Case}. */
    public static final String WORK_ITEM_TYPE = "System.WorkItemType";
    /** Team project that owns the queried work item. */
    public static final String TEAM_PROJECT = "System.TeamProject";

    private final String name;

    private Field(
            String name
    ) {
        this.name = name.replaceAll("^\\[?(.+?)]?$", "[$1]");
    }

    /**
     * Creates a WIQL field reference, adding square brackets when necessary.
     * For example, both {@code System.Id} and {@code [System.Id]} render as
     * {@code [System.Id]}.
     *
     * @param name Azure DevOps reference name
     * @return normalized field token
     */
    public static Field of(
            String name
    ) {
        return new Field(name);
    }

    @Override
    public String toString() {
        return name;
    }

}
