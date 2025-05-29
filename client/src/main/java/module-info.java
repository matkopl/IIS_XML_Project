module hr.algebra.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires java.xml;
    requires xmlrpc.client;
    requires xmlrpc.common;


    opens hr.algebra.client to javafx.fxml;
    exports hr.algebra.client;
    exports hr.algebra.client.controller;
    opens hr.algebra.client.controller to javafx.fxml;
}