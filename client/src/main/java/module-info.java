module hr.algebra.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires xmlrpc.client;
    requires xmlrpc.common;
    requires org.json;
    requires com.fasterxml.jackson.databind;
    requires java.xml;


    opens hr.algebra.client to javafx.fxml;
    exports hr.algebra.client;
    exports hr.algebra.client.controller;
    opens hr.algebra.client.controller to javafx.fxml;
}