/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.files;


import iti.wakamiti.api.plan.DataTable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.temporal.ValueRange;

public class DataTableHelperTest {

    private static Logger log = LoggerFactory.getLogger("iti.wakamiti.test");

    @Test
    public void newInstanceWithSuccess() {
        // prepare
        DataTable dataTable = new DataTable(new String[][]{{"from", "to", "value"}, {"6", "7", "aaa"}, {"0", "1", "12"}, {"3", "4", "cdt"}});

        // act
        DataTableHelper helper = new DataTableHelper(dataTable);
        log.debug("Result: {}", helper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void newInstanceWhenColumnsAreNotCorrectWithError() {
        // prepare
        DataTable dataTable = new DataTable(new String[][]{{"position", "value"}, {"6", "aaa"}, {"0", "12"}, {"3", "cdt"}});

        // act
        try {
            DataTableHelper helper = new DataTableHelper(dataTable);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test(expected = NumberFormatException.class)
    public void newInstanceWhenFromColumnIsNotNumberWithError() {
        // prepare
        DataTable dataTable = new DataTable(new String[][]{{"from", "to", "value"}, {"a", "7", "aaa"}, {"0", "1", "12"}, {"3", "4", "cdt"}});

        // act
        try {
            DataTableHelper helper = new DataTableHelper(dataTable);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test(expected = NumberFormatException.class)
    public void newInstanceWhenToolumnIsNotNumberWithError() {
        // prepare
        DataTable dataTable = new DataTable(new String[][]{{"from", "to", "value"}, {"6", "a", "aaa"}, {"0", "1", "12"}, {"3", "4", "cdt"}});

        // act
        try {
            DataTableHelper helper = new DataTableHelper(dataTable);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test
    public void getRangeWhenFromIsBiggerThanToWithError() {
        // prepare
        DataTable dataTable = new DataTable(new String[][]{{"from", "to", "value"}, {"6", "7", "aaa"}, {"0", "1", "12"}, {"3", "4", "cdt"}});
        DataTableHelper helper = new DataTableHelper(dataTable);

        // act
        ValueRange result = helper.getRange(0);
        log.debug("Result: {}", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRangeWhenFromIsSmallerThanToWithError() {
        // prepare
        DataTable dataTable = new DataTable(new String[][]{{"from", "to", "value"}, {"6", "3", "aaa"}, {"0", "1", "12"}, {"3", "4", "cdt"}});
        DataTableHelper helper = new DataTableHelper(dataTable);

        // act
        try {
            helper.getRange(0);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }
}