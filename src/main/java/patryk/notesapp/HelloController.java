package patryk.notesapp;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import patryk.notesapp.model.Note;
import com.fasterxml.jackson.databind.ObjectMapper;
import patryk.notesapp.service.NoteService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HelloController {

    private final NoteService noteService = new NoteService();

    @FXML
    private Button addToDo;
    @FXML
    private VBox toDoBox;
    @FXML
    private VBox doneBox;
    @FXML
    private VBox inProgressBox;

    @FXML
    void addToDoNote(ActionEvent event) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("New note");
            dialog.setHeaderText("Insert note:");
            Optional<String> result = dialog.showAndWait();
        result.ifPresent(noteText -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Note.fxml"));
                Node noteNode = loader.load();
                NoteController noteController = loader.getController();
                noteNode.setUserData(noteController);
                Note note = new Note();
                note.setContent(noteText);
                note.setStatus("TODO");
                noteController.setNote(note);
                VBox.setMargin(noteNode, new Insets(0,0,10,0));
                toDoBox.getChildren().add(noteNode);
                noteService.save(toDoBox, inProgressBox, doneBox);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void addNoteToBoard(Note note) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Note.fxml"));
            Node noteNode = loader.load();
            NoteController noteController = loader.getController();
            noteNode.setUserData(noteController);
            noteController.setNote(note);
            VBox.setMargin(noteNode, new Insets(0,0,10,0));
            switch (note.getStatus()) {
                case "TODO":
                    toDoBox.getChildren().add(noteNode);
                    break;
                case "IN_PROGRESS":
                    inProgressBox.getChildren().add(noteNode);
                    break;
                case "DONE":
                    doneBox.getChildren().add(noteNode);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    void initialize() {
        List<Note> loadedNotes = noteService.loadNotesFromFile("notes.json");
        for (Note note : loadedNotes) {
            addNoteToBoard(note);
        }
        setupDragAndDrop(toDoBox);
        setupDragAndDrop(inProgressBox);
        setupDragAndDrop(doneBox);
    }
    private void setupDragAndDrop(VBox box) {
        box.setOnDragDetected(event -> {
            Node node = (Node) event.getTarget();
            if (node != null && node.getParent().equals(box)) {
                SnapshotParameters sp = new SnapshotParameters();
                WritableImage snapshot = node.snapshot(sp, null);
                node.setStyle("");
                node.setVisible(false);
                Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("Note");
                db.setDragView(snapshot);
                db.setDragViewOffsetX(event.getX());
                db.setDragViewOffsetY(event.getY());
                db.setContent(content);
                event.consume();
            }
        });
        box.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        box.setOnDragDropped(event -> {
            Node noteNode = (Node) event.getGestureSource();
            NoteController noteController = (NoteController) noteNode.getUserData(); // zakładam, że używasz metody setUserData w NoteController, aby przechowywać odwołanie do samego siebie
            Note note = noteController.getNote();
            if(box == toDoBox) {
                note.setStatus("TODO");
            } else if(box == inProgressBox) {
                note.setStatus("IN_PROGRESS");
            } else if(box == doneBox) {
                note.setStatus("DONE");
            }
            VBox sourceBox = (VBox) noteNode.getParent();
            if (!sourceBox.equals(box)) {
                sourceBox.getChildren().remove(noteNode);
                box.getChildren().add(noteNode);
            }
            noteNode.setVisible(true);
            event.setDropCompleted(true);
            noteService.save(toDoBox,inProgressBox,doneBox);
            event.consume();
        });
        box.setOnDragDone(event -> {
            Node node = (Node) event.getGestureSource();
            if (event.getTransferMode() != TransferMode.MOVE) {
                node.setVisible(true);
            }
        });
    }
}
