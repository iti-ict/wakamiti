/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;

import java.util.List;
import java.util.Objects;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase extends BaseModel {

    public static final String CATEGORY = "Microsoft.TestCaseCategory";

    @JsonProperty
    private String id;
    private String name;
    private String description;
    private String tag;
    private int order;
    private TestSuite suite;
    private List<PointAssignment> pointAssignments;
    private transient PlanNodeSnapshot metadata;

    public TestCase id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public TestCase name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public TestCase description(String description) {
        this.description = description;
        return this;
    }

    public String description() {
        return description;
    }

    public TestCase tag(String tag) {
        this.tag = tag;
        return this;
    }

    public String tag() {
        return tag;
    }

    public TestCase order(int order) {
        this.order = order;
        return this;
    }

    public int order() {
        return order;
    }

    public TestCase suite(TestSuite suite) {
        this.suite = suite;
        return this;
    }

    public TestSuite suite() {
        return suite;
    }

    public TestCase pointAssignments(List<PointAssignment> pointAssignments) {
        this.pointAssignments = pointAssignments;
        return this;
    }

    public List<PointAssignment> pointAssignments() {
        return pointAssignments;
    }

    public TestCase metadata(PlanNodeSnapshot metadata) {
        this.metadata = metadata;
        return this;
    }

    public PlanNodeSnapshot metadata() {
        return metadata;
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{tag};
    }

    public String identifier() {
        return this.name.replaceAll("^\\[([^]]+)].+$", "$1");
    }

    public boolean isDifferent(TestCase testCase) {
        return Objects.nonNull(testCase) &&
                (!this.name.equals(testCase.name) || !Objects.equals(this.description, testCase.description)
        );
    }

    public TestCase merge(TestCase testCase) {
        this.suite = testCase.suite;
        this.name = testCase.name;
        this.description = testCase.description;
        return this;
    }

}
