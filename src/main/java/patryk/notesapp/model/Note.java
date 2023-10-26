package patryk.notesapp.model;

public class Note {

        private String content;
        private String status;
        private String category;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;
        if (!content.equals(note.content)) return false;
        return category.equals(note.category);
    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + category.hashCode();
        return result;
    }
}
