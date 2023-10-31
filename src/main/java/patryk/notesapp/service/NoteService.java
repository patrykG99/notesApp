package patryk.notesapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import patryk.notesapp.NoteController;
import patryk.notesapp.model.Data;
import patryk.notesapp.model.Note;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NoteService {

    private String dataPath = System.getProperty("user.home") + "\\notes.json";


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
        // Wczytuj wszystkie notatki i etykiety z VBox'ów
        List<Note> notesFromToDoBox = getNotesFromColumn(toDoBox);
        List<Note> notesFromInProgressBox = getNotesFromColumn(inProgressBox);
        List<Note> notesFromDoneBox = getNotesFromColumn(doneBox);
        List<String> labelsFromCategories = getLabelsFromBox(categories);

        // Dodaj wczytane notatki do obecnych notatek w currentData
        currentData.getNotes().addAll(notesFromToDoBox);
        currentData.getNotes().addAll(notesFromInProgressBox);
        currentData.getNotes().addAll(notesFromDoneBox);

        // Dodaj wczytane etykiety do obecnych etykiet w currentData
        currentData.getLabels().addAll(labelsFromCategories);


         currentData.setNotes(new ArrayList<>(new HashSet<>(currentData.getNotes())));
         currentData.setLabels(new ArrayList<>(new HashSet<>(currentData.getLabels())));

        // Zapisz zaktualizowane dane do pliku
        saveDataToFile(currentData.getNotes(), currentData.getLabels(), dataPath);
    }

    private List<Note> getNotesFromColumn(VBox column) {
        List<Note> notes = new ArrayList<>();
        for (Node node : column.getChildren()) {
            NoteController controller = (NoteController) node.getUserData();
            notes.add(controller.getNote());
        }
        return notes;
    }
    private List<String> getLabelsFromBox(VBox column){
        List<String> labels = new ArrayList<>();
        for (Node node : column.getChildren()) {
            if (node instanceof Label label) {
                labels.add(label.getText());
            }
        }
        return labels;
    }


    public Data readDataFromFile(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(filePath), Data.class);
        } catch (IOException e) {
            e.printStackTrace();  // Wydrukuj stos wywołań, aby dowiedzieć się więcej o błędzie
            System.out.println("Problem z odczytem danych z pliku: " + filePath);
            return new Data(new ArrayList<>(), new ArrayList<>());
        }
    }
}
