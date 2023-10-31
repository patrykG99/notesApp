package patryk.notesapp;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.event.ActionEvent;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import patryk.notesapp.model.Data;
import patryk.notesapp.model.Note;
import com.fasterxml.jackson.databind.ObjectMapper;
import patryk.notesapp.service.NoteService;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class HelloController {

    private final NoteService noteService = new NoteService();

    private String category ="default";
    private String dataPath = System.getProperty("user.home") + "\\notes.json";

    private List<String> selectedCategories = new ArrayList<>();



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
    private Button exitButton;
    @FXML
    private AnchorPane titleBox;
    @FXML
    private Button minimizeButton;

    @FXML
    private CheckBox showAllCheck;
    private List<String> existingLabels = new ArrayList<>();

    private List<Note> notesToShow = new ArrayList<>();
    private List<Note> allNotes = new ArrayList<>();



    @FXML
    void addToDoNote(ActionEvent event) {


        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("New note");



        TextArea noteTextField = new TextArea();
        noteTextField.setPromptText("Write your note here");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.setEditable(true);
        categoryComboBox.getItems().addAll(existingLabels);  // Przykładowe kategorie


        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Note:"), 0, 0);
        grid.add(noteTextField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryComboBox, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == buttonTypeOk) {
                return new Pair<>(noteTextField.getText(), categoryComboBox.getValue());
            }
            return null;
        });
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("addNoteDialog");
        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(noteAndCategory -> {
            try {
                String noteText = noteAndCategory.getKey();
                String categoryNote = noteAndCategory.getValue();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Note.fxml"));
                Node noteNode = loader.load();
                NoteController noteController = loader.getController();
                noteNode.setUserData(noteController);
                Note note = new Note();
                note.setContent(noteText);
                note.setStatus("TODO");
                note.setCategory(categoryNote);
                noteController.setNote(note);
                Button deleteButton = createDeleteButton(noteNode, toDoBox);
                ((AnchorPane) noteNode).getChildren().add(deleteButton);
                VBox.setMargin(noteNode, new Insets(0,0,10,0));
                toDoBox.getChildren().add(noteNode);
                Data currentData = noteService.readDataFromFile(dataPath);
                if (currentData == null) {
                    currentData = new Data(new ArrayList<>(), new ArrayList<>());
                }
                noteService.save(toDoBox, inProgressBox, doneBox, categoryBox,currentData);
                allNotes.add(note);
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
            Data currentData = noteService.readDataFromFile(dataPath);
            if (currentData == null) {
                currentData = new Data(new ArrayList<>(), new ArrayList<>());
            }
            currentData.getNotes().removeIf(existingNote -> existingNote.equals(note));

            noteService.save(toDoBox, inProgressBox, doneBox, categoryBox,currentData);
        });
        Image trashImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/trashcan.png")));
        ImageView trashImageView = new ImageView(trashImage);
        trashImageView.setFitHeight(20);
        trashImageView.setFitWidth(20);
        deleteButton.setGraphic(trashImageView);
        deleteButton.getStyleClass().add("deleteNote");
        return deleteButton;
    }

    private void loadLabel(String text) {

        Label label = new Label(text);
        label.setOnMouseClicked(e -> {
            Label clickedLabel = (Label) e.getSource();

                if(clickedLabel != null && !selectedCategories.contains(clickedLabel.getText())){
                    selectedCategories.add(clickedLabel.getText());


                    clickedLabel.getStyleClass().addAll("label-selected", "no-hover");
                }
                else if(clickedLabel != null){
                    selectedCategories.remove(clickedLabel.getText());
                    clickedLabel.getStyleClass().removeAll("label-selected", "no-hover");
                }




            toDoBox.getChildren().clear();
            inProgressBox.getChildren().clear();
            doneBox.getChildren().clear();
            showNotes(null);
        });
        label.setMaxWidth(200);
        label.setMaxHeight(150);
        label.setMinHeight(100);

        label.getStyleClass().add("category-label");
        categoryBox.getChildren().add(label);
    }

    @FXML
    void initialize() {
        allNotes = noteService.readDataFromFile(dataPath).getNotes();
        List<String> labels = noteService.readDataFromFile(dataPath).getLabels();

        if(!labels.isEmpty()){
            existingLabels = labels;
            for(String label : labels){
                loadLabel(label);
            }
        }
        toDoBox.setBorder(new Border(new BorderStroke(Color.web("#34495E"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 1, 0, 0))));
        categoryScroll.setFitToHeight(true);
        categoryScroll.setFitToWidth(true);
        setButtonsGraphic();
        setupDragAndDrop(toDoBox);
        setupDragAndDrop(inProgressBox);
        setupDragAndDrop(doneBox);
    }

    private void setButtonsGraphic(){
        Image pencilIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pencil.png")));
        ImageView pencilImageView = new ImageView(pencilIcon);
        pencilImageView.setFitHeight(20);
        pencilImageView.setFitWidth(20);
        addToDo.setGraphic(pencilImageView);
        Image iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/baseline_close_white_18dp.png")));
        ImageView exitImage = new ImageView(iconImage);
        exitImage.setFitHeight(20);
        exitImage.setFitWidth(20);
        exitButton.setGraphic(exitImage);
        Image iconImageMini = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/baseline_minimize_white_18dp.png")));
        ImageView miniImage = new ImageView(iconImageMini);
        miniImage.setFitHeight(20);
        miniImage.setFitWidth(20);
        minimizeButton.setGraphic(miniImage);
        exitButton.setOnAction(e ->{
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            if(s != null)
                s.close();
        });
        exitButton.setMaxHeight(Double.MAX_VALUE);
        minimizeButton.setOnAction(e ->{
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            if(s != null){
                s.setIconified(true);

            }

        });
    }


    @FXML
    void showNotes(ActionEvent event) {
        if(showAllCheck.isSelected()){
            toDoBox.getChildren().clear();
            inProgressBox.getChildren().clear();
            doneBox.getChildren().clear();
            notesToShow = allNotes;
            System.out.println("selected");

        }
        else if(!showAllCheck.isSelected()){
            toDoBox.getChildren().clear();
            inProgressBox.getChildren().clear();
            doneBox.getChildren().clear();
            notesToShow = allNotes.stream().filter(x -> selectedCategories.contains(x.getCategory())).toList();
            System.out.println("not");

        }
        for (Note note : notesToShow) {
            addNoteToBoard(note);
        }
    }




    private void setupDragAndDrop(VBox box) {

        box.setOnDragDetected(event -> {
            Node node = (Node) event.getTarget();
            if (node != null && node.getParent().equals(box)) {
                node.setStyle("-fx-effect: none;");
                SnapshotParameters sp = new SnapshotParameters();
                sp.setFill(Color.TRANSPARENT);
                WritableImage snapshot = node.snapshot(sp, null);

                node.setVisible(false);
                Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("Note");
                db.setDragView(snapshot);
                db.setDragViewOffsetX(event.getX()-20);
                db.setDragViewOffsetY(event.getY()-node.getLayoutY());
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
            Data currentData = noteService.readDataFromFile(dataPath);
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
            noteNode.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 30, 0.7, 3, 3)");
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
