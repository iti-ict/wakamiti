package iti.kukumo.database.dataset;

import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;



public class CompletableDataSet extends DataSet {

    private final Map<String,Object> nonNullableColumns = new LinkedHashMap<>();
    private final Map<String,Integer> nonNullableColumnsWithType;
    private final DataSet wrapped;
    
    
    
    
    public CompletableDataSet(DataSet wrapped, Map<String,Integer> nonNullableColumnsWithType) {
        super(wrapped.table(),wrapped.origin());
        this.wrapped = wrapped;
        this.nonNullableColumnsWithType = nonNullableColumnsWithType;
        List<String> originalColumnList = Arrays.asList(wrapped.columns());
        for (Entry<String, Integer> nonNullableColumnWithType : nonNullableColumnsWithType.entrySet()) {
            if (!containsIgnoringCase(originalColumnList,nonNullableColumnWithType.getKey())) {
                this.nonNullableColumns.put(nonNullableColumnWithType.getKey(), nonNullValueFor(nonNullableColumnWithType.getValue()));
            }
        }
        this.columns = Stream.concat(
                Stream.of(wrapped.columns()), 
                nonNullableColumns.keySet().stream()
            ).toArray(String[]::new);
    }

    



    @Override
    public Object rowValue(int columnIndex) {
        Object value = getIgnoringCase(nonNullableColumns,columns[columnIndex]);
        return value == null ? wrapped.rowValue(columnIndex) : value;
    }

    
    private Object getIgnoringCase(Map<String, Object> map, String key) {
        return map.getOrDefault(key, map.get(key.toUpperCase()));
    }


    @Override
    public boolean nextRow() {
        return wrapped.nextRow();
    }
    
    
    @Override
    public void close() throws IOException {
        wrapped.close();        
    }

    
    @Override
    public DataSet copy() throws IOException {
    	return new CompletableDataSet(wrapped.copy(), nonNullableColumnsWithType);
    }
    
    private static Object nonNullValueFor(Integer sqlType) {
        switch (sqlType) {
            case Types.BIT:
            case Types.BIGINT:
            case Types.BOOLEAN:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.FLOAT:
            case Types.REAL:
            case Types.DOUBLE:
            case Types.DECIMAL:
            case Types.NUMERIC:
                return 1;
            case Types.CHAR:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.VARCHAR:
                return "A";
            case Types.DATE:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return Date.valueOf(LocalDate.of(2000, 1, 1));
            case Types.TIME:
            case Types.TIME_WITH_TIMEZONE:
                return Time.valueOf(LocalTime.of(12,0));
            default:
                return 0;
        }
    }

    
   
}
