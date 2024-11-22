/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSuite extends BaseModel {

    public static final String CATEGORY = "Microsoft.TestSuiteCategory";

    @JsonProperty
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private Type suiteType;
    @JsonProperty("parentSuite")
    private TestSuite parent;
    private boolean hasChildren;
    private int order;

    public TestSuite id(String id) {
        this.id = id;
        return this;
    }

    public String id() {
        return id;
    }

    public TestSuite name(String name) {
        this.name = name;
        return this;
    }

    public String name() {
        return name;
    }

    public TestSuite suiteType(Type suiteType) {
        this.suiteType = suiteType;
        return this;
    }

    public Type suiteType() {
        return suiteType;
    }

    public TestSuite parent(TestSuite parent) {
        this.parent = parent;
        return this;
    }

    public TestSuite parent() {
        return parent;
    }

    public TestSuite hasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
        return this;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public TestSuite order(int order) {
        this.order = order;
        return this;
    }

    public int order() {
        return order;
    }

    public TestSuite root() {
        return Optional.ofNullable(parent).map(TestSuite::root).orElse(this);
    }

    public TestSuite root(TestSuite root) {
        if (!root().equals(root))
            root().parent(root);
        return this;
    }

    public Path asPath() {
        return Optional.ofNullable(parent).map(TestSuite::asPath).map(p -> p.resolve(name)).orElse(Path.of(name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(asPath());
    }

    @Override
    public String toString() {
        return "TestSuite["+asPath()+"]";
    }

    public enum Type {
        @JsonProperty("staticTestSuite")
        STATIC_TEST_SUITE,
        @JsonProperty("dynamicTestSuite")
        DYNAMIC_TEST_SUITE,
        @JsonProperty("requirementTestSuite")
        REQUIREMENT_TEST_SUITE,;

    }

}
