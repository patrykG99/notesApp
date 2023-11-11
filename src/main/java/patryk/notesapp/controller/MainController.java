package patryk.notesapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import patryk.notesapp.model.Data;
import patryk.notesapp.model.Note;
import patryk.notesapp.service.NoteService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private static final String NOTE_FXML = "/patryk/notesapp/Note.fxml";
    private final NoteService noteService = new NoteService();
    private final String dataPath = System.getProperty("user.home") + "\\notes.json";
    private final List<String> selectedCategories = new ArrayList<>();
    @FXML
    private Button addToDo;
    @FXML
    private VBox toDoBox;
    @FXML
    private VBox doneBox;
    @FXML
    private VBox inProgressBox;
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
    private double xOffset = 0;
    private double yOffset = 0;

    @FXML
    void addToDoNote() {
        Optional<Pair<String, String>> result = showNoteDialog("New note", "", "");
        result.ifPresent(noteAndCategory -> {
            try {
                String noteText = noteAndCategory.getKey();
                String categoryNote = noteAndCategory.getValue();
                FXMLLoader loader = new FXMLLoader(getClass().getResource(NOTE_FXML));
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
                noteNode.getStyleClass().add("toDoNote");
                saveData(noteNode,toDoBox);
                allNotes.add(note);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to load Note.fxml", e);
                showErrorDialog();
            }
        });
    }
    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Operation Failed");
        alert.setContentText("There was an error performing the operation. Please try again.");
        alert.showAndWait();
    }

    private Optional<Pair<String, String>> showNoteDialog(String title, String content, String category) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        TextArea noteTextField = new TextArea();
        noteTextField.setText(content); // Set the existing content if editing
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.setEditable(true);
        categoryComboBox.getItems().addAll(existingLabels);
        categoryComboBox.setValue(category); // Set the existing category if editing

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
        dialog.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/patryk/notesapp/styles.css")).toExternalForm());
        dialog.getDialogPane().getStyleClass().add("addNoteDialog");

        return dialog.showAndWait();
    }
    private void saveData(Node element, VBox container){

        container.getChildren().add(element);
        Data currentData = noteService.readDataFromFile(dataPath);
        if (currentData == null) {
            currentData = new Data(new ArrayList<>(), new ArrayList<>());
        }
        noteService.save(toDoBox, inProgressBox, doneBox, categoryBox, currentData);
    }
    @FXML
    void addCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New category");
        dialog.setHeaderText("Insert category name:");
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 20) {
                String truncated = newValue.substring(0, 20);
                dialog.getEditor().setText(truncated);
            }
        });
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::loadLabel);
    }
    private void addNoteToBoard(Note note) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(NOTE_FXML));
            Node noteNode = loader.load();
            NoteController noteController = loader.getController();
            noteNode.setUserData(noteController);
            noteController.setNote(note);

            VBox box = switch (note.getStatus()) {
                case "TODO" -> toDoBox;
                case "IN_PROGRESS" -> inProgressBox;
                case "DONE" -> doneBox;
                default -> throw new IllegalStateException("Nieznany status notatki: " + note.getStatus());
            };
            configureNoteAppearance(note, noteNode, box);
            VBox.setMargin(noteNode, new Insets(0, 0, 10, 0));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load Note.fxml", e);
            showErrorDialog();
        }
    }
    private Button createDeleteButton(Node noteNode, VBox container) {
        Button deleteButton = new Button();
        AnchorPane.setLeftAnchor(deleteButton,0.0);
        deleteButton.setOnAction(e -> {
            NoteController noteController = (NoteController) noteNode.getUserData();
            Note note = noteController.getNote();
            container.getChildren().remove(noteNode);
            Data currentData = noteService.readDataFromFile(dataPath);
            if (currentData == null) {
                currentData = new Data(new ArrayList<>(), new ArrayList<>());
            }
            currentData.getNotes().removeIf(existingNote -> existingNote.equals(note));
            allNotes.remove(note);
            noteService.save(toDoBox, inProgressBox, doneBox, categoryBox, currentData);
        });
        Image trashImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/trashcan.png")));
        ImageView trashImageView = new ImageView(trashImage);
        trashImageView.setFitHeight(20);
        trashImageView.setFitWidth(20);
        deleteButton.setGraphic(trashImageView);
        deleteButton.getStyleClass().add("deleteNote");
        return deleteButton;
    }

    private Button createEditButton(Node noteNode){
        Button editButton = new Button();
        AnchorPane.setRightAnchor(editButton,0.0);

        Image trashImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/pencil.png")));
        ImageView trashImageView = new ImageView(trashImage);
        trashImageView.setFitHeight(20);
        trashImageView.setFitWidth(20);
        editButton.setGraphic(trashImageView);
        editButton.getStyleClass().add("deleteNote");
        NoteController noteController = (NoteController) noteNode.getUserData();
        Note note = noteController.getNote();

        editButton.setOnAction(e ->{



            Optional<Pair<String, String>> result = showNoteDialog("Edit note", note.getContent(), note.getCategory());

            result.ifPresent(noteAndCategory -> {
                    String noteText = noteAndCategory.getKey();
                    String categoryNote = noteAndCategory.getValue();
                    note.setContent(noteText);
                    note.setCategory(categoryNote);
                Data currentData = noteService.readDataFromFile(dataPath);
                if (currentData == null) {
                    currentData = new Data(new ArrayList<>(), new ArrayList<>());
                }

                noteService.save(toDoBox, inProgressBox, doneBox, categoryBox, currentData);



            });

        });
        return editButton;
    }
    private void configureNoteAppearance(Note note, Node noteNode, VBox box) {
        noteNode.getStyleClass().removeAll("toDoNote", "inProgressNote", "doneNote", "note");
        Button deleteButton = createDeleteButton(noteNode, box);
        Button editButton = createEditButton(noteNode);
        ((AnchorPane) noteNode).getChildren().add(deleteButton);
        ((AnchorPane) noteNode).getChildren().add(editButton);
        String statusClass = switch (note.getStatus()) {
            case "TODO" -> "toDoNote";
            case "IN_PROGRESS" -> "inProgressNote";
            case "DONE" -> "doneNote";
            default -> throw new IllegalStateException("Unknown note status: " + note.getStatus());
        };
        noteNode.getStyleClass().addAll("note", statusClass);
        box.getChildren().add(noteNode);
    }

    private void loadLabel(String text) {
        VBox categoryContainer = new VBox();
        Label label = new Label(text);
        Button deleteCategory = new Button();
        deleteCategory.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setHeaderText("Are you sure you want to remove this category with all its notes?");
            ButtonType buttonTypeOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(buttonTypeOk, ButtonType.CANCEL);
            Optional<ButtonType> result = dialog.showAndWait();
            result.ifPresent(buttonType -> {
                if (buttonType == buttonTypeOk) {
                    allNotes = allNotes.stream().filter(note -> !Objects.equals(note.getCategory(), text)).toList();
                    existingLabels.remove(text);
                    ((VBox) categoryContainer.getParent()).getChildren().remove(categoryContainer);
                    Data currentData = noteService.readDataFromFile(dataPath);
                    if (currentData == null) {
                        currentData = new Data(new ArrayList<>(), new ArrayList<>());
                    }
                    currentData.getNotes().removeIf(existingNote -> Objects.equals(existingNote.getCategory(), text));
                    currentData.getLabels().removeIf(existingLabel -> Objects.equals(existingLabel, text));
                    noteService.save(toDoBox, inProgressBox, doneBox, categoryBox, currentData);
                }
            });
        });
        categoryContainer.setOnMouseClicked(e -> {
            Label clickedLabel = (Label) categoryContainer.getChildren().get(1);
            if (clickedLabel != null && !selectedCategories.contains(clickedLabel.getText())) {
                selectedCategories.add(clickedLabel.getText());
                clickedLabel.getParent().getStyleClass().addAll("label-selected", "no-hover");
            } else if (clickedLabel != null) {
                selectedCategories.remove(clickedLabel.getText());
                clickedLabel.getParent().getStyleClass().removeAll("label-selected", "no-hover");
            }
            toDoBox.getChildren().clear();
            inProgressBox.getChildren().clear();
            doneBox.getChildren().clear();
            showNotes();
        });
        label.setMaxWidth(180);
        label.setMaxHeight(150);
        label.setMinHeight(100);
        Image trashImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/outline_delete_white_24dp.png")));
        ImageView trashImageView = new ImageView(trashImage);
        trashImageView.setFitHeight(20);
        trashImageView.setFitWidth(20);
        VBox.setMargin(deleteCategory, new Insets(0, 0, 0, 0));
        deleteCategory.setPrefHeight(20);
        deleteCategory.setPrefWidth(20);
        deleteCategory.setMinHeight(Button.USE_PREF_SIZE);
        deleteCategory.setMinWidth(Button.USE_PREF_SIZE);
        deleteCategory.setTranslateX(-10);
        deleteCategory.setGraphic(trashImageView);
        deleteCategory.getStyleClass().add("deleteCategoryButton");
        categoryContainer.getStyleClass().add("category-label");
        label.setStyle("-fx-text-fill: white");
        categoryContainer.getChildren().add(deleteCategory);
        categoryContainer.getChildren().add(label);
        saveData(categoryContainer, categoryBox);
    }
    @FXML
    void initialize() {
        allNotes = noteService.readDataFromFile(dataPath).getNotes();
        List<String> labels = noteService.readDataFromFile(dataPath).getLabels();

        if (!labels.isEmpty()) {
            existingLabels = labels;
            for (String label : labels) {
                loadLabel(label);
            }
        }
        categoryScroll.setFitToHeight(true);
        categoryScroll.setFitToWidth(true);
        setButtonsGraphic();
        setupDragAndDrop(toDoBox);
        setupDragAndDrop(inProgressBox);
        setupDragAndDrop(doneBox);
        titleBox.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBox.setOnMouseDragged(event -> {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
    private void setButtonsGraphic() {
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
        exitButton.setOnAction(e -> {
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            if (s != null)
                s.close();
        });
        exitButton.setMaxHeight(Double.MAX_VALUE);
        minimizeButton.setOnAction(e -> {
            Stage s = (Stage) ((Node) e.getSource()).getScene().getWindow();
            if (s != null) {
                s.setIconified(true);
            }
        });
    }
    @FXML
    void showNotes() {
        if (showAllCheck.isSelected()) {
            toDoBox.getChildren().clear();
            inProgressBox.getChildren().clear();
            doneBox.getChildren().clear();
            notesToShow = allNotes;
        } else if (!showAllCheck.isSelected()) {
            toDoBox.getChildren().clear();
            inProgressBox.getChildren().clear();
            doneBox.getChildren().clear();
            notesToShow = allNotes.stream().filter(x -> selectedCategories.contains(x.getCategory())).toList();
        }
        for (Note note : notesToShow) {
            addNoteToBoard(note);
        }
    }
    private void setupDragAndDrop(VBox box) {
        box.setOnDragDetected(event -> {
            Node node = (Node) event.getTarget();
            if (node instanceof Text)
                node = node.getParent();

            if (node != null && node.getParent().equals(box)) {
                node.setStyle("-fx-effect: none");
                SnapshotParameters sp = new SnapshotParameters();
                sp.setFill(Color.TRANSPARENT);
                WritableImage snapshot = node.snapshot(sp, null);
                node.setVisible(false);
                Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("Note");
                db.setContent(content);
                Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
                double offsetX = event.getSceneX() - nodeBounds.getMinX();
                double offsetY = event.getSceneY() - nodeBounds.getMinY();
                db.setDragView(snapshot, offsetX, offsetY);

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
            noteNode.getStyleClass().removeAll("toDoNote", "inProgressNote", "doneNote");
            if (box == toDoBox) {
                note.setStatus("TODO");
            } else if (box == inProgressBox) {
                note.setStatus("IN_PROGRESS");
            } else if (box == doneBox) {
                note.setStatus("DONE");
            }
            configureNoteAppearance(note, noteNode, box);
            VBox sourceBox = (VBox) noteNode.getParent();
            if (!sourceBox.equals(box)) {
                sourceBox.getChildren().remove(noteNode);
                box.getChildren().add(noteNode);
            }
            noteNode.setStyle("-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 30, 0.7, 3, 3)");
            noteNode.setVisible(true);
            event.setDropCompleted(true);
            noteService.save(toDoBox, inProgressBox, doneBox, categoryBox, currentData);
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