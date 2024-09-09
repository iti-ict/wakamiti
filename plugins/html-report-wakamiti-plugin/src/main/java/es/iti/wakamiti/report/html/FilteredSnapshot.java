/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.report.html;


import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.plan.Result;
import es.iti.wakamiti.report.html.factory.DurationTemplateNumberFormatFactory;
import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isEmpty;


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
    private String m;
    private String e;
    private Result r;
    private Map<Result, Long> tr;
    private List<FilteredSnapshot> c = new LinkedList<>();

    public static FilteredSnapshot of(PlanNodeSnapshot snapshot) {
        FilteredSnapshot filteredSnapshot = new FilteredSnapshot();
        filteredSnapshot.t = snapshot.getNodeType();
        filteredSnapshot.i = Strings.isNotBlank(snapshot.getId()) ?
                snapshot.getId().replaceAll("^#", "") : null;
        filteredSnapshot.n = snapshot.getName();
        filteredSnapshot.k = snapshot.getKeyword();
        filteredSnapshot.l = !isEmpty(snapshot.getDescription()) ? snapshot.getDescription() : null;
        filteredSnapshot.g = !isEmpty(snapshot.getTags()) ? snapshot.getTags() : null;
        filteredSnapshot.w = snapshot.getDuration() != null ?
                DurationTemplateNumberFormatFactory.format(snapshot.getDuration()) : null;
        filteredSnapshot.p = snapshot.getDocument();
        filteredSnapshot.o = snapshot.getDocumentType();
        filteredSnapshot.d = snapshot.getDataTable();
        filteredSnapshot.m = snapshot.getNodeType().isAnyOf(NodeType.STEP, NodeType.VIRTUAL_STEP) ?
                snapshot.getErrorMessage() : null;
        filteredSnapshot.e = snapshot.getNodeType().isAnyOf(NodeType.STEP, NodeType.VIRTUAL_STEP)
                && snapshot.getResult() == Result.ERROR ? snapshot.getErrorTrace() : null;
        filteredSnapshot.r = snapshot.getResult();
        filteredSnapshot.tr = MapUtils.isNotEmpty(snapshot.getTestCaseResults())
                && snapshot.getNodeType() == NodeType.AGGREGATOR ? snapshot.getTestCaseResults() : null;
        filteredSnapshot.c = !isEmpty(snapshot.getChildren()) ? of(snapshot.getChildren()) : null;

        return filteredSnapshot;
    }

    public static List<FilteredSnapshot> of(List<PlanNodeSnapshot> snapshots) {
        return snapshots.stream().map(FilteredSnapshot::of).collect(Collectors.toList());
    }

    public NodeType getT() {
        return t;
    }

    public String getI() {
        return i;
    }

    public String getN() {
        return n;
    }

    public String getK() {
        return k;
    }

    public List<String> getL() {
        return l;
    }

    public List<String> getG() {
        return g;
    }

    public String getW() {
        return w;
    }

    public String getP() {
        return p;
    }

    public String getO() {
        return o;
    }

    public String[][] getD() {
        return d;
    }

    public String getM() {
        return m;
    }

    public String getE() {
        return e;
    }

    public Result getR() {
        return r;
    }

    public Map<Result, Long> getTr() {
        return tr;
    }

    public List<FilteredSnapshot> getC() {
        return c;
    }
}
