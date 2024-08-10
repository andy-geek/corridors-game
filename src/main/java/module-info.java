module com.davydov.corridorsgame {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.davydov.corridorsgame.client to javafx.fxml;
    exports com.davydov.corridorsgame.client;
}