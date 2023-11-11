package patryk.notesapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import patryk.notesapp.controller.NoteController;
import patryk.notesapp.model.Data;
import patryk.notesapp.model.Note;

import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class NoteService {
    private final String dataPath = System.getProperty("user.home") + "\\notes.json";
    private static final Logger LOGGER = Logger.getLogger(NoteService.class.getName());


    public void saveDataToFile(List<Note> notes, List<String> labels, String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        Data data = new Data(notes, labels);
        try {
            mapper.writeValue(new File(filePath), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(VBox toDoBox, VBox inProgressBox, VBox doneBox, VBox categories, Data currentData) {
        List<Note> allNotes = new ArrayList<>(); // A list to hold all the notes for saving
        allNotes.addAll(getNotesFromColumn(toDoBox));
        allNotes.addAll(getNotesFromColumn(inProgressBox));
        allNotes.addAll(getNotesFromColumn(doneBox));
        List<String> labelsFromCategories = getLabelsFromBox(categories);

        for (Note newNote : allNotes) {
            Note existingNote = findNoteById(currentData.getNotes(), newNote.getId());
            if (existingNote != null) {
                existingNote.setContent(newNote.getContent());
                existingNote.setCategory(newNote.getCategory());
            } else {
                currentData.getNotes().add(newNote);
            }
        }

        currentData.setLabels(new ArrayList<>(new HashSet<>(labelsFromCategories)));

        saveDataToFile(currentData.getNotes(), currentData.getLabels(), dataPath);
    }

    private Note findNoteById(List<Note> notes, String id) {
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                return note;
            }
        }
        return null;
    }

    private List<Note> getNotesFromColumn(VBox column) {
        List<Note> notes = new ArrayList<>();
        for (Node node : column.getChildren()) {
            NoteController controller = (NoteController) node.getUserData();
            notes.add(controller.getNote());
        }
        return notes;
    }

    private List<String> getLabelsFromBox(VBox column) {
        return column.getChildren().stream()
                .filter(node -> node instanceof VBox)
                .map(vbox -> (VBox) vbox)
                .filter(vbox -> !vbox.getChildren().isEmpty() && vbox.getChildren().get(1) instanceof Label)
                .map(vbox -> (Label) vbox.getChildren().get(1))
                .map(Label::getText)
                .collect(Collectors.toList());
    }


    public Data readDataFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(filePath), Data.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new Data(new ArrayList<>(), new ArrayList<>());
        }
    }
}
