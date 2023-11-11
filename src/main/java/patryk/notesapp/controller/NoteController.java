package patryk.notesapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import patryk.notesapp.model.Note;

public class NoteController {
    private Note note;

    @FXML
    private Text noteContent;
    @FXML
    private Label categoryLabel;



    @FXML
    void handleDrag(MouseEvent event) {

    }

    public void setNoteContent(String text){
        if (this.note != null) {
            this.note.setContent(text);

        }
        noteContent.setText(text);
        noteContent.setUserData(this);

    }

    public void setNote(Note note) {
        this.note = note;

        noteContent.textProperty().bind(note.contentProperty());
        categoryLabel.textProperty().bind(note.categoryProperty());
    }

    public Note getNote() {
        return note;
    }
}
