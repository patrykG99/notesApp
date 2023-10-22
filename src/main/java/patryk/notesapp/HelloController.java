package patryk.notesapp;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import patryk.notesapp.model.Data;
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

    private String category ="default";



    @FXML
    private Button addToDo;
    @FXML
    private VBox toDoBox;
    @FXML
    private VBox doneBox;
    @FXML
    private VBox inProgressBox;
    @FXML
    private Button categoryAdd;

    @FXML
    private VBox categoryBox;
    @FXML
    private ScrollPane categoryScroll;

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
                note.setCategory(category);
                noteController.setNote(note);
                Button deleteButton = createDeleteButton(noteNode, toDoBox);
                ((AnchorPane) noteNode).getChildren().add(deleteButton);
                VBox.setMargin(noteNode, new Insets(0,0,10,0));
                toDoBox.getChildren().add(noteNode);
                Data currentData = noteService.readDataFromFile("notes.json");
                if (currentData == null) {
                    currentData = new Data(new ArrayList<>(), new ArrayList<>());
                }
                noteService.save(toDoBox, inProgressBox, doneBox, categoryBox,currentData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void addCategory(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New category");
        dialog.setHeaderText("Insert category name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::loadLabel);
    }



    private void addNoteToBoard(Note note) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Note.fxml"));
            Node noteNode = loader.load();
            NoteController noteController = loader.getController();
            noteNode.setUserData(noteController);
            noteController.setNote(note);

            Button deleteButton;
            switch (note.getStatus()) {
                case "TODO":
                    deleteButton = createDeleteButton(noteNode, toDoBox);
                    toDoBox.getChildren().add(noteNode);
                    break;
                case "IN_PROGRESS":
                    deleteButton = createDeleteButton(noteNode, inProgressBox);
                    inProgressBox.getChildren().add(noteNode);
                    break;
                case "DONE":
                    deleteButton = createDeleteButton(noteNode, doneBox);
                    doneBox.getChildren().add(noteNode);
                    break;
                default:
                    throw new IllegalStateException("Nieznany status notatki: " + note.getStatus());
            }
            ((AnchorPane) noteNode).getChildren().add(deleteButton);

            VBox.setMargin(noteNode, new Insets(0,0,10,0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Button createDeleteButton(Node noteNode, VBox container) {
        Button deleteButton = new Button();
        deleteButton.setOnAction(e -> {
            NoteController noteController = (NoteController) noteNode.getUserData();
            Note note = noteController.getNote();
            System.out.println("Usuwanie notatki...");
            container.getChildren().remove(noteNode);
            Data currentData = noteService.readDataFromFile("notes.json");
            if (currentData == null) {
                currentData = new Data(new ArrayList<>(), new ArrayList<>());
            }
            currentData.getNotes().removeIf(existingNote -> existingNote.equals(note));

            noteService.save(toDoBox, inProgressBox, doneBox, categoryBox,currentData);
        });
        Image trashImage = new Image(getClass().getResourceAsStream("/trashcan.png"));
        ImageView trashImageView = new ImageView(trashImage);
        trashImageView.setFitHeight(20);
        trashImageView.setFitWidth(20);
        deleteButton.setGraphic(trashImageView);
        deleteButton.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-border-color: transparent; -fx-border-radius: 5; -fx-background-radius: 5;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #00ACC1; -fx-text-fill: white; -fx-border-color: transparent; -fx-border-radius: 5; -fx-background-radius: 5;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #00ACC1     ; -fx-text-fill: white; -fx-border-color: transparent; -fx-border-radius: 5; -fx-background-radius: 5;"));
        return deleteButton;
    }

    private void loadLabel(String text) {

        Label label = new Label(text);
        label.setOnMouseClicked(e -> {
            Label clickedLabel = (Label) e.getSource();
            category = clickedLabel.getText();
            toDoBox.getChildren().clear();
            inProgressBox.getChildren().clear();
            doneBox.getChildren().clear();
            List<Note> loadedNotes = noteService.readDataFromFile("notes.json").getNotes().stream().filter(x -> x.getCategory().equals(category)).toList();
            for (Note note : loadedNotes) {
                addNoteToBoard(note);
            }
            System.out.println(category);
        });
        label.setMaxWidth(200);
        label.setMaxHeight(150);
        label.setMinHeight(100);

        label.getStyleClass().add("category-label");
        categoryBox.getChildren().add(label);
    }


    @FXML
    void initialize() {
        List<Note> loadedNotes = noteService.readDataFromFile("notes.json").getNotes().stream().filter(e -> e.getCategory().equals(category)).toList();
        List<String> labels = noteService.readDataFromFile("notes.json").getLabels();
        if(!loadedNotes.isEmpty()){
            for (Note note : loadedNotes) {
                addNoteToBoard(note);
            }
        }
        if(!labels.isEmpty()){
            for(String label : labels){
                loadLabel(label);
            }
        }

        toDoBox.setBorder(new Border(new BorderStroke(Color.web("#34495E"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 1, 0, 0))));
        Image pencilIcon = new Image(getClass().getResourceAsStream("/pencil.png"));
        ImageView pencilImageView = new ImageView(pencilIcon);
        pencilImageView.setFitHeight(20);
        pencilImageView.setFitWidth(20);
        addToDo.setGraphic(pencilImageView);

        categoryScroll.setFitToHeight(true);
        categoryScroll.setFitToWidth(true);


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
            NoteController noteController = (NoteController) noteNode.getUserData();
            Note note = noteController.getNote();
            Data currentData = noteService.readDataFromFile("notes.json");
            if (currentData == null) {
                currentData = new Data(new ArrayList<>(), new ArrayList<>());
            }
            currentData.getNotes().removeIf(existingNote -> existingNote.equals(note));
            Button deleteButton = null;
            if(box == toDoBox) {
                deleteButton = createDeleteButton(noteNode, toDoBox);
                note.setStatus("TODO");
            } else if(box == inProgressBox) {
                deleteButton = createDeleteButton(noteNode, inProgressBox);
                note.setStatus("IN_PROGRESS");
            } else if(box == doneBox) {
                deleteButton = createDeleteButton(noteNode, doneBox);
                note.setStatus("DONE");
            }
            ((AnchorPane) noteNode).getChildren().add(deleteButton);
            VBox sourceBox = (VBox) noteNode.getParent();
            if (!sourceBox.equals(box)) {
                sourceBox.getChildren().remove(noteNode);
                box.getChildren().add(noteNode);
            }
            noteNode.setVisible(true);
            event.setDropCompleted(true);
            noteService.save(toDoBox, inProgressBox, doneBox, categoryBox,currentData);
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
