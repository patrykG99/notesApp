module patryk.notesapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.logging;


    opens patryk.notesapp to javafx.fxml;
    exports patryk.notesapp;
    exports patryk.notesapp.model to com.fasterxml.jackson.databind;
}