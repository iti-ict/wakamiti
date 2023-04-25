module kukumo.db.test {
    requires junit;
    requires kukumo.db;
    requires spring.jdbc;
    requires testcontainers;
    requires kukumo.api;
    requires kukumo.core;
    requires poi.ooxml;
    requires org.assertj.core;
    requires jsqlparser;
    requires org.junit.jupiter.api;

    exports iti.kukumo.database.test.dialect;
}
