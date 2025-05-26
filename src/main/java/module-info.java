module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.mohyla.embroidery to javafx.fxml;
    exports org.mohyla.embroidery;
}