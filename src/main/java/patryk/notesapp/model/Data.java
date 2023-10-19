package patryk.notesapp.model;

import java.util.List;

public class Data {

    private List<Note> notes;
    private List<String> labels;

    public List<Note> getNotes() {
        return notes;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Data(List<Note> notes, List<String> labels) {
        this.notes = notes;
        this.labels = labels;
    }
    public Data(){

    }
}
