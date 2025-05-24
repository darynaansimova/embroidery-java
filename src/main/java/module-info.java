module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.mohyla.embroidery to javafx.fxml;
    exports org.mohyla.embroidery;
}