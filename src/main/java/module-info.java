module patryk.notesapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.logging;

    opens patryk.notesapp.model to com.fasterxml.jackson.databind;
    opens patryk.notesapp to javafx.fxml;
    exports patryk.notesapp;
    exports patryk.notesapp.model to com.fasterxml.jackson.databind;
    exports patryk.notesapp.controller;
    opens patryk.notesapp.controller to javafx.fxml;
}