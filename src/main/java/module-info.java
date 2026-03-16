module org.example._erronka_java {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires javafx.graphics;
    requires java.net.http;
    requires com.google.gson;
    requires javafx.base;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires unirest.java;
    requires tools.jackson.databind;

    opens Pantailak to javafx.fxml;
    opens Klaseak to javafx.base, com.google.gson;
    opens icons;
    opens css;
    opens services to com.google.gson;

    exports Klaseak;
    exports Pantailak;
    exports services;

}