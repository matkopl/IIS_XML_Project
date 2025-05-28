module hr.algebra.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens hr.algebra.client to javafx.fxml;
    exports hr.algebra.client;
}