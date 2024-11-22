/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure.api.model.query;


import es.iti.wakamiti.azure.api.model.query.criteria.Criteria;
import es.iti.wakamiti.azure.api.model.query.criteria.Expression;
import org.junit.Test;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static es.iti.wakamiti.api.util.MapUtils.*;
import static es.iti.wakamiti.azure.api.model.query.Field.*;
import static es.iti.wakamiti.azure.api.model.query.OrderElement.asc;
import static es.iti.wakamiti.azure.api.model.query.OrderElement.desc;
import static es.iti.wakamiti.azure.api.model.query.criteria.Criteria.field;
import static org.assertj.core.api.Assertions.assertThat;


public class WorkItemsQueryTest {

    @Test
    public void tesQueryWhenSingleWhereWithSuccess() {

        Map<String, BiFunction<Criteria, String, Expression>> criterias = mapEntries(
                entry("= '%s'", Criteria::isEqualsTo),
                entry("<> '%s'", Criteria::isNotEqualsTo),
                entry("< '%s'", Criteria::isLessThan),
                entry("<= '%s'", Criteria::isLessThanOrEqualTo),
                entry("> '%s'", Criteria::isGreaterThan),
                entry(">= '%s'", Criteria::isGreaterThanOrEqualTo),
                entry("IN ('%s')", Criteria::isIn),
                entry("NOT IN ('%s')", Criteria::isNotIn),
                entry("CONTAINS '%s'", Criteria::isContains),
                entry("NOT CONTAINS '%s'", Criteria::isNotContains),
                entry("UNDER '%s'", Criteria::isUnder),
                entry("NOT UNDER '%s'", Criteria::isNotUnder)
        );
        Query query = new WorkItemsQuery().select(Field.of("System.Id")).where(field("System.Type"));

        for (Map.Entry<String, BiFunction<Criteria, String, Expression>> entry : criterias.entrySet()) {
            String value = "A";
            query.where(entry.getValue().apply(field(TYPE), value));
            String result = String.format(entry.getKey(), value);
            assertThat(query.toString())
                    .isEqualTo("SELECT [System.Id] FROM WorkItems WHERE [System.Type] " + result);
        }

        Map<String, Function<Criteria, Expression>> criterias2 = map(
                "IS NULL", Criteria::isNull,
                "IS NOT NULL", Criteria::isNotNull
        );

        for (Map.Entry<String, Function<Criteria, Expression>> entry : criterias2.entrySet()) {
            query.where(entry.getValue().apply(field(TYPE)));
            String result = entry.getKey();
            assertThat(query)
                    .hasToString("SELECT [System.Id] FROM WorkItems WHERE [System.Type] " + result);
        }
    }

    @Test
    public void testQueryWhenGroupWhereWithSuccess() {
        WorkItemsQuery query = new WorkItemsQuery();
        query.select("System.Id", "System.State")
                .where(field(TYPE).isEqualsTo("A").and(
                        field("System.State").isEqualsTo("Y")
                                .or(field(STATE).isEqualsTo("@project"))
                ).andEver(field(TITLE).isEqualsTo("Y"))
                        .orEver(field(DESCRIPTION).isEqualsTo("@project")))
                .asof("2024-05-03");

        assertThat(query).hasToString(
                "SELECT [System.Id], [System.State] FROM WorkItems"
                        + " WHERE [System.Type] = 'A'"
                        + " AND ([System.State] = 'Y' OR [System.State] = @project)"
                        + " AND EVER [System.Title] = 'Y'"
                        + " OR EVER [System.Description] = @project"
                        + " ASOF '2024-05-03'");
    }

    @Test
    public void testQueryWhenOrderByWithSuccess() {
        WorkItemLinksQuery query = new WorkItemLinksQuery().mode(Query.Mode.RECURSIVE);
        query.select();

        query.orderBy(asc("System.Id"), desc("System.Type"));
        assertThat(query).hasToString(
                "SELECT [System.Id] FROM WorkItemLinks "
                        + "ORDER BY [System.Id] ASC, [System.Type] DESC "
                        + "MODE (Recursive)"
        );

        query.orderBy(ID, TYPE);
        assertThat(query).hasToString(
                "SELECT [System.Id] FROM WorkItemLinks "
                        + "ORDER BY [System.Id], [System.Type] "
                        + "MODE (Recursive)"
        );

        query.orderBy("System.Id", "System.Type");
        assertThat(query).hasToString(
                "SELECT [System.Id] FROM WorkItemLinks "
                        + "ORDER BY [System.Id], [System.Type] "
                        + "MODE (Recursive)"
        );
    }
}
