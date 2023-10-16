package patryk.notesapp;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import patryk.notesapp.model.Note;

public class NoteController {
    private Note note;

    @FXML
    private Text noteContent;

    @FXML
    void handleDrag(MouseEvent event) {
        System.out.println("dsada");
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
        noteContent.setText(note.getContent());
    }

    public Note getNote() {
        return note;
    }
}
