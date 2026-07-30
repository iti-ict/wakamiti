/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model;


import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Provides the Test Suite functionality used by Wakamiti.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TestSuite extends BaseModel {

    /** Azure DevOps work-item category used to recognize Test Suite types. */
    public static final String CATEGORY = "Microsoft.TestSuiteCategory";
    /** Entity used to preserve literal slashes inside one suite path segment. */
    public static final String SLASH_CODE = "&#47;";

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

    /**
     * Assigns the numeric suite identifier returned by Azure Test Plans.
     *
     * @param id Azure test-suite identifier
     * @return this suite
     */
    public TestSuite id(
            String id
    ) {
        this.id = id;
        return this;
    }

    /**
     * @return Azure test-suite identifier
     */
    public String id() {
        return id;
    }

    /**
     * Sets the suite name displayed below its parent in Azure Test Plans.
     *
     * @param name suite name within its parent
     * @return this suite
     */
    public TestSuite name(
            String name
    ) {
        this.name = name;
        return this;
    }

    /**
     * @return suite name without ancestor path segments
     */
    public String name() {
        return name;
    }

    /**
     * Sets the Azure suite classification that determines how tests are selected.
     *
     * @param suiteType static, dynamic or requirement-backed Azure type
     * @return this suite
     */
    public TestSuite suiteType(
            Type suiteType
    ) {
        this.suiteType = suiteType;
        return this;
    }

    /**
     * @return Azure suite creation strategy
     */
    public Type suiteType() {
        return suiteType;
    }

    /**
     * Associates the immediate parent required to reconstruct the suite hierarchy.
     *
     * @param parent immediate parent suite, or {@code null} for a root
     * @return this suite
     */
    public TestSuite parent(
            TestSuite parent
    ) {
        this.parent = parent;
        return this;
    }

    /**
     * @return immediate parent suite, or {@code null} for a root
     */
    public TestSuite parent() {
        return parent;
    }

    /**
     * Records whether Azure reports descendants without loading those descendants.
     *
     * @param hasChildren whether Azure reports nested suites
     * @return this suite
     */
    public TestSuite hasChildren(
            boolean hasChildren
    ) {
        this.hasChildren = hasChildren;
        return this;
    }

    /**
     * @return whether the suite is known to contain child suites
     */
    public boolean hasChildren() {
        return hasChildren;
    }

    /**
     * Sets the ordering position used when publishing this suite among siblings.
     *
     * @param order position among sibling suites
     * @return this suite
     */
    public TestSuite order(
            int order
    ) {
        this.order = order;
        return this;
    }

    /**
     * @return position among sibling suites
     */
    public int order() {
        return order;
    }

    /**
     * Traverses parent links to find the topmost suite.
     *
     * @return hierarchy root, or this suite when it has no parent
     */
    public TestSuite root() {
        return Optional.ofNullable(parent).map(TestSuite::root).orElse(this);
    }

    /**
     * Reparents the current hierarchy beneath a required root when necessary.
     *
     * @param root required Azure plan root suite
     * @return this suite
     */
    public TestSuite root(
            TestSuite root
    ) {
        if (!root().equals(root)) {
            root().parent(root);
        }
        return this;
    }

    /**
     * Represents the complete suite hierarchy as a path.
     * Literal slashes in suite names are encoded as {@link #SLASH_CODE} so they
     * cannot be confused with hierarchy separators.
     *
     * @return path from the root suite to this suite
     */
    public Path asPath() {
        String aux = name.replace("/", SLASH_CODE);
        return Optional.ofNullable(parent).map(TestSuite::asPath).map(p -> p.resolve(aux)).orElse(Path.of(aux));
    }

    @Override
    protected Object[] hashValues() {
        return new Object[]{asPath()};
    }

    @Override
    public String toString() {
        return "TestSuite[" + asPath() + "]";
    }

    /**
     * Defines the values supported by Type.
     */
    public enum Type {

        /** A static test suite that contains a fixed set of test cases. */
        @JsonProperty("staticTestSuite")
        STATIC_TEST_SUITE,
        /** A dynamic test suite whose test cases are determined by a query. */
        @JsonProperty("dynamicTestSuite")
        DYNAMIC_TEST_SUITE,
        /** A requirement-based test suite linked to a work-item requirement. */
        @JsonProperty("requirementTestSuite")
        REQUIREMENT_TEST_SUITE;

    }

}
