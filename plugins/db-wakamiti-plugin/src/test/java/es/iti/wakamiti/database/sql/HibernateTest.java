package es.iti.wakamiti.database.sql;


import com.mdimension.jchronic.Chronic;
import es.iti.wakamiti.database.LevenshteinDistance;
import es.iti.wakamiti.database.Similarity;
import es.iti.wakamiti.database.StringDistance;
import es.iti.wakamiti.database.domain.Record;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.davidmoten.rxjava3.jdbc.ConnectionProvider;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import org.davidmoten.rxjava3.jdbc.Database;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class HibernateTest {

    private static final String CONNECTION_URL = "jdbc:mysql://localhost:3306/test";
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "password";
    private static final String TABLE = "data";

    private static final Map<String, String> VALUES = new LinkedHashMap<>();

    @Before
    public  void setup() {
        VALUES.put("datetime", "2014-12-02 04:01:16");
        VALUES.put("channel", "2");
        VALUES.put("value", "48.64");
        VALUES.put("something", "565c92ecda");
    }

    @Test
    public void testRx() throws SQLException, InterruptedException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD)) {
            long init = System.currentTimeMillis();
            // process input data
            processData(TABLE, connection, VALUES);
            AtomicLong end = new AtomicLong(System.currentTimeMillis());
            System.out.println("Data processed in " + (end.get() -init) + " milliseconds");

            String[] values = VALUES.values().toArray(new String[0]);

            init = System.currentTimeMillis();
            try (Database db = Database.fromBlocking(new ConnectionProvider() {
                @NotNull
                @Override
                public Connection get() {
                    return connection;
                }

                @Override
                public void close() {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            })) {
                db
                        .select(String.format("SELECT %s FROM %s", String.join(",", VALUES.keySet()), TABLE))
//                        .fetchSize(100)
//                        .transactedValuesOnly()
//                        .valuesOnly()

                        .get(rs -> {
                            ResultSetMetaData metadata = rs.getMetaData();
                            String[] row = new String[metadata.getColumnCount()];
                            for (int c = 1; c <= metadata.getColumnCount(); c++) {
                                String column = metadata.getColumnName(c);
                                JDBCType type = JDBCType.valueOf(metadata.getColumnType(c));
                                String value = rs.getString(column);
                                if (rs.wasNull()) {
                                    row[c-1] = "";
                                    continue;
                                }
                                switch (type) {
                                    case DATE: case TIMESTAMP: case TIME: case TIME_WITH_TIMEZONE: case TIMESTAMP_WITH_TIMEZONE:
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        calendar.setLenient(true);
                                        String date = DateTools.dateToString(rs.getTimestamp(column, calendar), DateTools.Resolution.MILLISECOND);
                                        row[c-1] = date;
                                        break;

                                    default:
                                        row[c-1] = value;
                                }
                            }
                            return row;
                        })

                        .doOnTerminate(db::close)

                        .subscribeOn(Schedulers.trampoline())

//                        .blockingStream()
//                        .parallel()
                        .map(map -> new Record(map, IntStream.range(0, VALUES.size())
                                .mapToDouble(i -> new StringDistance().score(values[i], map[i]))
                                .sum() / VALUES.size()))
                        .reduce((rec1, rec2) -> rec1.score() > rec2.score() ? rec1 : rec2)
//                        .max((rec1, rec2) -> Comparator.<Double>naturalOrder().compare(rec1.score(), rec2.score()))
//                        .sorted((rec1, rec2) -> Comparator.<Double>naturalOrder().reversed().compare(rec1.score(), rec2.score()))
//                        .firstElement()
                        .subscribe((rec) -> {
                            end.set(System.currentTimeMillis());
                            System.out.println(rec);
                        })
                        .dispose();
                        //.ifPresent(System.out::println)
//                    .forEach(System.out::println)
                ;
//                end.set(System.currentTimeMillis());
//                TimeUnit.SECONDS.sleep(30);

                System.out.println("Searched in " + (end.get() - init) + " milliseconds");
            }
        }
    }

    @Test
    public void testLucene() throws IOException, SQLException {
        // https://stackoverflow.com/questions/16203118/how-can-i-implement-the-tf-idf-and-cosine-similarity-in-lucene

        try (ByteBuffersDirectory dir = new ByteBuffersDirectory()){
//        try (FSDirectory dir = FSDirectory.open(Path.of("./target/.db"))){

            Connection connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD);

            // process input data
            processData(TABLE, connection, VALUES);

            // create indexes
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter indexWriter = new IndexWriter(dir, indexWriterConfig);
            System.out.println("Indexing to directory '" + dir + "'...");
            long init = System.currentTimeMillis();
            int indexedDocumentCount = indexOocs(TABLE, indexWriter, connection, VALUES);
            long end = System.currentTimeMillis();
            indexWriter.close();
            connection.close();
            System.out.println(indexedDocumentCount + " records have been indexed successfully in " + (end - init) + " milliseconds");

            // search data
            init = System.currentTimeMillis();
            Optional<Map<String, String>> record = search(dir, VALUES);
            end = System.currentTimeMillis();
            System.out.println("Searched in " + (end-init) + " milliseconds");

            // cleanup
//            cleanUp(dir.getDirectory());

            assertThat(record).isNotEmpty();
            assertThat(record.get())
                    .containsAllEntriesOf(Map.of(
                            "datetime", "20141203040116000",
                            "channel", "2",
                            "value", "48.46",
                            "something", "565c9ecdaa"
                    ));
        }
    }

    @Test
    public void testLevenshteinDistance() throws SQLException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD)) {
            // process input data
            processData(TABLE, connection, VALUES);

            long init = System.currentTimeMillis();
            Optional<Map<String, String>> record = search(TABLE, connection, VALUES, new LevenshteinDistance());
            long end = System.currentTimeMillis();
            System.out.println("Searched in " + (end-init) + " milliseconds");

            assertThat(record).isNotEmpty();
            assertThat(record.get())
                    .containsAllEntriesOf(Map.of(
                            "datetime", "20141203040116000",
                            "channel", "2",
                            "value", "48.46",
                            "something", "565c9ecdaa"
                    ));
        }
    }


    @Test
    public void testStringDistance() throws SQLException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD)) {
            // process input data
            processData(TABLE, connection, VALUES);

            long init = System.currentTimeMillis();
            Optional<Map<String, String>> record = search(TABLE, connection, VALUES, new StringDistance());
            long end = System.currentTimeMillis();
            System.out.println("Searched in " + (end-init) + " milliseconds");

            assertThat(record).isNotEmpty();
            assertThat(record.get())
                    .containsAllEntriesOf(Map.of(
                            "datetime", "20141203040116000",
                            "channel", "2",
                            "value", "48.46",
                            "something", "565c9ecdaa"
                    ));
        }
    }

    @Test
    public void testStringDistance2() throws SQLException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD)) {
            // process input data
            processData(TABLE, connection, VALUES);

            long init = System.currentTimeMillis();
            Optional<Map<String, String>> record = search(TABLE, connection, VALUES, (value1, value2) -> {
                int maxLength = Math.max(value1.length(), value2.length());
                double distance = new org.apache.commons.text.similarity.LevenshteinDistance().apply(value1, value2);
                //Can't divide by 0
                if (maxLength == 0) return 1.0d;
                return (maxLength - distance) / (double) maxLength;
            });
            long end = System.currentTimeMillis();
            System.out.println("Searched in " + (end-init) + " milliseconds");

            assertThat(record).isNotEmpty();
            assertThat(record.get())
                    .containsAllEntriesOf(Map.of(
                            "datetime", "20141203040116000",
                            "channel", "2",
                            "value", "48.46",
                            "something", "565c9ecdaa"
                    ));
        }
    }

    private void processData(String table, Connection conn, Map<String, String> values) throws SQLException {
        DatabaseMetaData md = conn.getMetaData();
        try (ResultSet rs = md.getColumns(conn.getCatalog(), conn.getSchema(), table, null)) {
            while (rs != null && rs.next()) {
                String column = rs.getString("COLUMN_NAME");
                if (values.containsKey(column)) {
                    if (values.get(column) == null) {
                        values.put(column, "");
                        continue;
                    }
                    switch (JDBCType.valueOf(rs.getInt("DATA_TYPE"))) {
                        case BIT: case BOOLEAN:
                            values.put(column, String.valueOf(Boolean.parseBoolean(values.get(column))));
                            break;
                        case TINYINT: case BIGINT: case INTEGER: case SMALLINT:
                            values.put(column, new BigInteger(values.get(column)).toString());
                            break;
                        case DECIMAL: case DOUBLE: case FLOAT:
                            values.put(column, new BigDecimal(values.get(column)).toString());
                            break;
                        case DATE: case TIMESTAMP: case TIME: case TIME_WITH_TIMEZONE: case TIMESTAMP_WITH_TIMEZONE:
                            values.put(column, DateTools.dateToString(
                                    Chronic.parse(values.get(column))
                                            .getBeginCalendar().getTime(), DateTools.Resolution.MILLISECOND));
                    }
                }
            }
        }
    }

    private Optional<Map<String, String>> search(String table, Connection conn, Map<String, String> values, Similarity similarity) throws SQLException {
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(200);
        String query = String.format("SELECT %s FROM %s", String.join(",", values.keySet()), table);
        System.out.println("Executing: " + query);
        Map<String, String> record = new LinkedHashMap<>();
        double score = 0;
        try (ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metadata = rs.getMetaData();
            while (rs.next()) {
                Map<String, String> current = new LinkedHashMap<>();
                for (int c = 1; c <= metadata.getColumnCount(); c++) {
                    String column = metadata.getColumnName(c);
                    JDBCType type = JDBCType.valueOf(metadata.getColumnType(c));

                    switch (type) {
                        case DATE: case TIMESTAMP: case TIME: case TIME_WITH_TIMEZONE: case TIMESTAMP_WITH_TIMEZONE:
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                            calendar.setLenient(true);
                            String date = DateTools.dateToString(rs.getTimestamp(column, calendar), DateTools.Resolution.MILLISECOND);
                            current.put(column, date);
                            break;
                        default:
                            current.put(column, rs.getString(column));
                    }
                    if (rs.wasNull()) {
                        current.put(column, "");
                    }
                }
                double currentScore = values.entrySet().stream()
                        .mapToDouble(e -> similarity.score(e.getValue(), current.get(e.getKey()))).sum() / values.size();
                //double currentScore = similarity.score(String.join("", values.values()), String.join("", current.values()));
                if (currentScore > score) {
                    score = currentScore;
                    record = current;
                }
            }
        }
        if (record.isEmpty()) {
            return Optional.empty();
        } else {
            System.out.println("Found similar records with score: " + score);
            return Optional.of(record);
        }
    }

    private int indexOocs(String table, IndexWriter writer, Connection conn, Map<String, String> values) throws SQLException, IOException {
        int i = 0;
        Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        String query = String.format("SELECT %s FROM %s", String.join(",", values.keySet()), table);
        System.out.println("Executing: " + query);
        try (ResultSet rs = stmt.executeQuery(query)) {
            rs.setFetchSize(100);
            ResultSetMetaData metadata = rs.getMetaData();
            while (rs.next()) {
                Document d = new Document();
                for (int c = 1; c <= metadata.getColumnCount(); c++) {
                    String column = metadata.getColumnName(c);
                    JDBCType type = JDBCType.valueOf(metadata.getColumnType(c));
                    String value = rs.getString(column);
                    if (rs.wasNull()) {
                        d.add(new StringField(column, "", Field.Store.YES));
                        continue;
                    }
                    switch (type) {
                        case DATE: case TIMESTAMP: case TIME: case TIME_WITH_TIMEZONE: case TIMESTAMP_WITH_TIMEZONE:
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
                            calendar.setLenient(true);
                            String date = DateTools.dateToString(rs.getTimestamp(column, calendar), DateTools.Resolution.MILLISECOND);
                            d.add(new StringField(column, date, Field.Store.YES));
                            break;
                        default:
                            d.add(new StringField(column, value, Field.Store.YES));
                    }
                }
                //KnnFloatVectorField
                writer.addDocument(d);
                i++;
            }
        }
        return i;

    }

    private void cleanUp(Path dir) throws IOException {
        long size = FileUtils.sizeOfDirectory(dir.toFile());
        String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int unitIndex = (int) (Math.log10(size) / 3);
        double unitValue = 1 << (unitIndex * 10);

        String readableSize = new DecimalFormat("#,##0.#")
                .format(size / unitValue) + " "
                + units[unitIndex];
        System.out.println("Cleaning '" + dir + "' (" + readableSize + ")");
        if (Files.exists(dir)) {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    public Optional<Map<String, String>> search(Directory dir, Map<String, String> values) throws IOException {
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        System.out.println("Searching -> " + values);

        // DistanceQuery

        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (String column : values.keySet()) {
            builder.add(new FuzzyQuery(new Term(column, values.get(column)), 2), BooleanClause.Occur.MUST);
        }
        BooleanQuery query = builder.build();

//        Query query = new FuzzyQuery(new Term("data", String.join("", values.values())), 2);

        TopDocs results = searcher.search(query, 1);
        ScoreDoc[] hits = results.scoreDocs;
        System.out.println(hits.length);

        if (hits.length > 0) {
            Document doc = searcher.doc(hits[0].doc);
            System.out.println("Found similar records with score: " + hits[0].score);

//            System.out.println("---------------------------- ");
//            System.out.println("data: " + doc.get("data"));
//            System.out.println("----------------------------");
//            System.out.println("Score: " + hits[0].score);
//            System.out.println("----------------------------");
            return Optional.of(values.keySet().stream().collect(Collectors.toMap(Function.identity(), doc::get)));
        }
        return Optional.empty();

    }
}
