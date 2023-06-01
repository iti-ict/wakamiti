module com.example.wakamitiemail {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.mail;
    requires activation;
    requires java.naming;
    requires java.xml;
    requires es.iti.wakamiti.api;
    requires junit;
    requires org.testng;

    opens es.iti.wakamiti.email to javafx.fxml;
    exports es.iti.wakamiti.email;
}