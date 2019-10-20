/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database.dataset;


import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class MultiDataSet implements Closeable, Iterable<DataSet> {

    private final List<DataSet> dataSets = new ArrayList<>();


    protected void addDataSet(DataSet dataSet) {
        dataSets.add(dataSet);
    }


    @Override
    public Iterator<DataSet> iterator() {
        return dataSets.iterator();
    }


    public abstract MultiDataSet copy() throws IOException;

}
