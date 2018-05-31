/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing.database;

import java.util.List;

public interface DataLoader {

    public List<DataSet> resolveData (Object...arguments);


}
