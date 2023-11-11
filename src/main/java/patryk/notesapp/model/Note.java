package patryk.notesapp.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public class Note {
    private final String id;
    private final StringProperty content = new SimpleStringProperty(this, "content", "");
    private final StringProperty status = new SimpleStringProperty(this, "status", "");
    private final StringProperty category = new SimpleStringProperty(this, "category", "");

    public Note() {
        this.id = UUID.randomUUID().toString();
    }

    public StringProperty contentProperty() {
        return content;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public String getContent() {
        return content.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getCategory() {
        return category.get();
    }

    public void setContent(String content) {
        this.content.set(content);
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;
        return id.equals(note.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
