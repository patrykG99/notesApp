module patryk.notesapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens patryk.notesapp to javafx.fxml;
    exports patryk.notesapp;
}