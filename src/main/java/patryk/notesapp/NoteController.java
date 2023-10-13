package patryk.notesapp;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class NoteController {
    @FXML
    void handleDrag(MouseEvent event) {
        System.out.println("dsada");
    }


    @FXML
    private Text noteContent;

    public void setNoteContent(String text){
        noteContent.setText(text);
    }
}
