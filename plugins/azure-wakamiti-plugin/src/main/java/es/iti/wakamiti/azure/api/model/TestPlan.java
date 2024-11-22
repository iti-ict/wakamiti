/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestPlan extends BaseModel {

    public static final String CATEGORY = "Microsoft.TestPlanCategory";

    @JsonProperty
    private String id;
    @JsonProperty(required = true)
    private String name;
    @JsonProperty(value = "areaPath", required = true)
    private String area;
    @JsonProperty(required = true)
    private String iteration;
    @JsonProperty
    private String state;
    @JsonProperty
    private TestSuite rootSuite;

    public TestPlan() { }

    public TestPlan(String name, Path area, Path iteration) {
        this.name = name;
        area(area);
        iteration(iteration);
    }

    public TestPlan id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public TestPlan name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public TestPlan area(Path area) {
        this.area = area.toString().replace("/", "\\");
        return this;
    }

    public Path area() {
        return Path.of(area);
    }

    public TestPlan iteration(Path iteration) {
        this.iteration = iteration.toString().replace("/", "\\");
        return this;
    }

    public Path iteration() {
        return Path.of(iteration);
    }

    public TestPlan state(String state) {
        this.state = state;
        return this;
    }

    public String state() {
        return state;
    }

    public TestPlan rootSuite(TestSuite rootSuite) {
        this.rootSuite = rootSuite;
        return this;
    }

    public TestSuite rootSuite() {
        return rootSuite;
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{name, area, iteration};
    }

}
