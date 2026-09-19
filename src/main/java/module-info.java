module com.example.africaadventures {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.example.africaadventures to javafx.fxml;
    exports com.example.africaadventures;
    exports com.example.viewmodel;
    opens com.example.viewmodel to javafx.fxml;
}