package hr.algebra.client.dto;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class SportDto {
    private final SimpleLongProperty id = new SimpleLongProperty();
    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty slug = new SimpleStringProperty();

    public SportDto(long id, String name, String slug) {
        this.id.set(id);
        this.name.set(name);
        this.slug.set(slug);
    }

    public SportDto() {
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public long getId() {
        return id.get();
    }

    public SimpleLongProperty idProperty() {
        return id;
    }

    public String getSlug() {
        return slug.get();
    }

    public SimpleStringProperty slugProperty() {
        return slug;
    }
}
