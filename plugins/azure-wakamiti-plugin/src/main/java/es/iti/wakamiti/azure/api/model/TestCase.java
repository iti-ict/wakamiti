/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import es.iti.wakamiti.api.plan.PlanNodeSnapshot;

import java.util.List;
import java.util.Objects;


public class TestCase extends BaseModel {

    public static final String CATEGORY = "Microsoft.TestCaseCategory";

    private String id;
    private String name;
    private String description;
    private String tag;
    private int order;
    private TestSuite suite;
    private List<PointAssignment> pointAssignments;
    private PlanNodeSnapshot metadata;

    public TestCase id(String id) {
        this.id = id;
        return this;
    }

    public TestCase name(String name) {
        this.name = name;
        return this;
    }

    public TestCase description(String description) {
        this.description = description;
        return this;
    }

    public TestCase tag(String tag) {
        this.tag = tag;
        return this;
    }

    public TestCase order(int order) {
        this.order = order;
        return this;
    }

    public TestCase suite(TestSuite suite) {
        this.suite = suite;
        return this;
    }

    public TestCase pointAssignments(List<PointAssignment> pointAssignments) {
        this.pointAssignments = pointAssignments;
        return this;
    }

    public TestCase metadata(PlanNodeSnapshot metadata) {
        this.metadata = metadata;
        return this;
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public String tag() {
        return tag;
    }

    public int order() {
        return order;
    }

    public TestSuite suite() {
        return suite;
    }

    public List<PointAssignment> PointAssignments() {
        return pointAssignments;
    }

    public PlanNodeSnapshot metadata() {
        return metadata;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    public boolean isDifferent(TestCase testCase) {
        return !this.suite.equals(testCase.suite) || !this.name.equals(testCase.name)
                || !this.description.equals(testCase.description);
    }

    public TestCase merge(TestCase testCase) {
        this.suite = testCase.suite;
        this.name = testCase.name;
        this.description = testCase.description;
        return this;
    }

}
