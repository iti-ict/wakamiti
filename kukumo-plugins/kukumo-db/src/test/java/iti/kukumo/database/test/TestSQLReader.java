package iti.kukumo.database.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import iti.kukumo.database.SQLReader;

public class TestSQLReader {

    
    @Test
    public void testSQLReader() throws IOException {
        StringBuilder script = new StringBuilder()
            .append(" insert a; insert b;  delete   \n")
            .append(" from table t; -- this is a line comment \n")
            .append(" update c  /* comment inside */ set a = 1; \r\n")
            .append(" drop database; /* multi line comment start \n")
            .append(" this line should be comment \n")
            .append(" end of comment */ insert d; \n")
        ;
        SQLReader reader = new SQLReader();
        List<String> statements = reader.parseStatements(new StringReader(script.toString()));
        assertThat(statements).containsExactly(
            "insert a",
            "insert b",
            "delete from table t",
            "update c  set a = 1",
            "drop database",
            "insert d"
        );
        
        
    }
    
}
