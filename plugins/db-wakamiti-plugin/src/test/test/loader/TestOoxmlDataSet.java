/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.database.test.loader;


import es.iti.wakamiti.database.dataset.DataSet;
import es.iti.wakamiti.database.dataset.OoxmlDataSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;


public class TestOoxmlDataSet {

    @Test
    public void testLoadXLS() throws InvalidFormatException, IOException {

        File file = new File("src/test/resources/data1.xlsx");
        try (OoxmlDataSet multiDataSet = new OoxmlDataSet(file, "#.*", "<null>")) {
            Iterator<DataSet> iterator = multiDataSet.iterator();

            DataSet clients = iterator.next();
            Assertions.assertThat(clients.collectColumns(","))
                .isEqualTo("id,first_name,second_name,active,birth_date");
            assertRow(clients, 1., "John", "Smith", true, "2000-10-30");
            assertRow(clients, 2., "Annie", "Hall", false, "2011-09-12");
            assertRow(clients, 3., "Bruce", null, true, "1982-12-31");
            Assertions.assertThat(clients.nextRow()).isFalse();

            DataSet city = iterator.next();
            Assertions.assertThat(city.collectColumns(",")).isEqualTo("id,name,zip_code");
            assertRow(city, 1., "New York", 46018.);
            assertRow(city, 2., "Baltimore", 55583.);
            assertRow(city, 3., "Tordesillas", 12356.);
            Assertions.assertThat(city.nextRow()).isFalse();

            DataSet userCity = iterator.next();
            Assertions.assertThat(userCity.collectColumns(",")).isEqualTo("client_id,city_id");
            assertRow(userCity, 1., 1.);
            assertRow(userCity, 1., 3.);
            assertRow(userCity, 2., 1.);
            assertRow(userCity, 2., 2.);
            assertRow(userCity, 3., 3.);
            Assertions.assertThat(userCity.nextRow()).isFalse();

            Assertions.assertThat(iterator.hasNext()).isFalse();
        }

    }


    private void assertRow(DataSet dataSet, Object... values) {
        Assertions.assertThat(dataSet.nextRow()).isTrue();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) {
                Assertions.assertThat(dataSet.rowValue(i)).isNull();
            } else {
                Assertions.assertThat(dataSet.rowValue(i)).isExactlyInstanceOf(values[i].getClass());
                Assertions.assertThat(dataSet.rowValue(i)).isEqualTo(values[i]);
            }
        }

    }
}