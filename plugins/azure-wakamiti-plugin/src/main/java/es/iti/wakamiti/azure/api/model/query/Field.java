/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


public class Field {

    public static final String ID = "System.Id";
    public static final String TITLE = "System.Title";
    public static final String DESCRIPTION = "System.Description";
    public static final String STEPS = "Microsoft.VSTS.TCM.Steps";
    public static final String PARAMETERS =  "Microsoft.VSTS.TCM.Parameters";
    public static final String LOCAL_DATASOURCE = "Microsoft.VSTS.TCM.LocalDataSource";
    public static final String TAGS = "System.Tags";
    public static final String AREA_PATH = "System.AreaPath";
    public static final String ITERATION_PATH = "System.IterationPath";
    public static final String STATE = "System.State";
    public static final String TYPE = "System.Type";
    public static final String WORK_ITEM_TYPE = "System.WorkItemType";
    public static final String TEAM_PROJECT = "System.TeamProject";

    private final String name;

    private Field(String name) {
        this.name = name.replaceAll("^\\[?(.+?)]?$", "[$1]");
    }

    public static Field of(String name) {
        return new Field(name);
    }

    @Override
    public String toString() {
        return name;
    }

}
