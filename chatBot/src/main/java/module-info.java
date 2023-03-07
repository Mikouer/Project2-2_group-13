module com.example.chatbot {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.sql;

    exports com.example.chatbot;
    opens com.example.chatbot to javafx.fxml;
    exports com.example.chatbot.controller;
    opens com.example.chatbot.controller to javafx.fxml;

}