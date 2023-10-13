package patryk.notesapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.Optional;

public class HelloController {



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
                    Node note = loader.load();
                    NoteController noteController = loader.getController();
                    noteController.setNoteContent(noteText);
                    VBox.setMargin(note, new Insets(0,0,10,0));
                    toDoBox.getChildren().add(note);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });



    }
    @FXML
    void initialize() {
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
            Node node = (Node) event.getGestureSource();
            VBox sourceBox = (VBox) node.getParent();
            if (!sourceBox.equals(box)) {
                sourceBox.getChildren().remove(node);
                box.getChildren().add(node);
            }
            node.setVisible(true);
            event.setDropCompleted(true);
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
