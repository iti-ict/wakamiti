module com.example.wakamitiemail {
    requires java.mail;
    requires activation;
    requires java.naming;
    requires java.xml;
    requires es.iti.wakamiti.api;
    requires junit;

    opens es.iti.wakamiti.email to javafx.fxml;
    exports es.iti.wakamiti.email;
}