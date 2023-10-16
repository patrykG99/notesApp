package patryk.notesapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import patryk.notesapp.NoteController;
import patryk.notesapp.model.Note;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NoteService {

    public void saveNotesToFile(List<Note> notes, String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(filePath), notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(VBox toDoBox, VBox inProgressBox, VBox doneBox){
        List<Note> allNotes = new ArrayList<>();
        allNotes.addAll(getNotesFromColumn(toDoBox));
        allNotes.addAll(getNotesFromColumn(inProgressBox));
        allNotes.addAll(getNotesFromColumn(doneBox));
        saveNotesToFile(allNotes, "notes.json");
    }

    private List<Note> getNotesFromColumn(VBox column) {
        List<Note> notes = new ArrayList<>();
        for (Node node : column.getChildren()) {
            NoteController controller = (NoteController) node.getUserData();
            notes.add(controller.getNote());
        }
        return notes;
    }

    public List<Note> loadNotesFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        List<Note> notes = new ArrayList<>();
        try {
            TypeReference<List<Note>> typeReference = new TypeReference<>() {};
            notes = mapper.readValue(new File(filePath), typeReference);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return notes;
    }
}
