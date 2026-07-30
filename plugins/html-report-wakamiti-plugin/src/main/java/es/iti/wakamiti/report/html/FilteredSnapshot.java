/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html;


import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.util.Strings;

import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.report.html.factory.DurationTemplateNumberFormatFactory;


/**
 * Compact projection of {@link PlanNodeSnapshot} used by the HTML report.
 * <p>
 * Field names are intentionally short because this model is serialized into
 * inline browser payload:
 * {@code t}=type, {@code i}=id, {@code n}=name, {@code k}=keyword,
 * {@code l}=description, {@code g}=tags, {@code w}=duration, {@code p}=doc,
 * {@code o}=doc type, {@code d}=data table, {@code m}=error message,
 * {@code e}=error trace, {@code r}=result, {@code tr}=test-case result counts,
 * {@code c}=children.
 * </p>
 */
public class FilteredSnapshot {

    private NodeType t;
    private String i;
    private String n;
    private String k;
    private List<String> l;
    private List<String> g;
    private String w;
    private String p;
    private String o;
    private String[][] d;
    private String response;
    private String m;
    private String e;
    private Result r;
    private Map<Result, Long> tr;
    private List<FilteredSnapshot> c = new LinkedList<>();

    /**
     * Projects a plan snapshot onto the compact JSON model embedded in the HTML
     * report.
     * <p>
     * Empty optional values are omitted, IDs lose a leading {@code #},
     * durations are preformatted, error traces are retained only for errored
     * steps, and children are converted recursively.
     *
     * @param snapshot source plan node
     * @return compact report representation
     */
    public static FilteredSnapshot of(
            PlanNodeSnapshot snapshot
    ) {
        FilteredSnapshot filteredSnapshot = new FilteredSnapshot();
        filteredSnapshot.t = snapshot.getNodeType();
        filteredSnapshot.i = Strings.isNotBlank(snapshot.getId())
                ? snapshot.getId().replaceAll("^#", "") : null;
        filteredSnapshot.n = snapshot.getName();
        filteredSnapshot.k = snapshot.getKeyword();
        filteredSnapshot.l = !isEmpty(snapshot.getDescription()) ? snapshot.getDescription() : null;
        filteredSnapshot.g = !isEmpty(snapshot.getTags()) ? snapshot.getTags() : null;
        filteredSnapshot.w = snapshot.getDuration() != null
                ? DurationTemplateNumberFormatFactory.format(snapshot.getDuration()) : null;
        filteredSnapshot.p = snapshot.getDocument();
        filteredSnapshot.o = snapshot.getDocumentType();
        filteredSnapshot.d = snapshot.getDataTable();
        filteredSnapshot.response = snapshot.getResponse();
        filteredSnapshot.m = snapshot.getNodeType().isAnyOf(NodeType.STEP, NodeType.VIRTUAL_STEP)
                ? snapshot.getErrorMessage() : null;
        filteredSnapshot.e = snapshot.getNodeType().isAnyOf(NodeType.STEP, NodeType.VIRTUAL_STEP)
                && snapshot.getResult() == Result.ERROR ? snapshot.getErrorTrace() : null;
        filteredSnapshot.r = snapshot.getResult();
        filteredSnapshot.tr = MapUtils.isNotEmpty(snapshot.getTestCaseResults())
                && snapshot.getNodeType() == NodeType.AGGREGATOR ? snapshot.getTestCaseResults() : null;
        filteredSnapshot.c = !isEmpty(snapshot.getChildren()) ? of(snapshot.getChildren()) : null;

        return filteredSnapshot;
    }

    /**
     * Converts snapshots to compact report nodes while preserving order.
     *
     * @param snapshots source plan nodes
     * @return compact nodes in the same order
     */
    public static List<FilteredSnapshot> of(
            List<PlanNodeSnapshot> snapshots
    ) {
        return snapshots.stream().map(FilteredSnapshot::of).collect(Collectors.toList());
    }

    /**
     * @return node type serialized under compact key {@code t}
     */
    public NodeType getT() {
        return t;
    }

    /**
     * @return node identifier without a leading {@code #}
     */
    public String getI() {
        return i;
    }

    /**
     * @return display name of the plan node
     */
    public String getN() {
        return n;
    }

    /**
     * @return localized Gherkin keyword
     */
    public String getK() {
        return k;
    }

    /**
     * @return description lines, or {@code null} when absent
     */
    public List<String> getL() {
        return l;
    }

    /**
     * @return node tags, or {@code null} when absent
     */
    public List<String> getG() {
        return g;
    }

    /**
     * @return preformatted execution duration
     */
    public String getW() {
        return w;
    }

    /**
     * @return doc-string content attached to the node
     */
    public String getP() {
        return p;
    }

    /**
     * @return declared doc-string media type
     */
    public String getO() {
        return o;
    }

    /**
     * @return data-table cells arranged by row and column
     */
    public String[][] getD() {
        return d;
    }

    /**
     * @return response or output captured during step execution
     */
    public String getResponse() {
        return response;
    }

    /**
     * @return step error message, or {@code null} for non-step nodes
     */
    public String getM() {
        return m;
    }

    /**
     * @return error trace retained only for steps with {@link Result#ERROR}
     */
    public String getE() {
        return e;
    }

    /**
     * @return execution result of the node
     */
    public Result getR() {
        return r;
    }

    /**
     * @return aggregate test-case counts grouped by result
     */
    public Map<Result, Long> getTr() {
        return tr;
    }

    /**
     * @return recursively filtered children, or {@code null} when absent
     */
    public List<FilteredSnapshot> getC() {
        return c;
    }

}
