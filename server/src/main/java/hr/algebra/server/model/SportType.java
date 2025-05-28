package hr.algebra.server.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SportType", propOrder = {"name", "slug"})
public class SportType {
    @XmlElement(namespace = "https://interoperabilnost.hr/sport", required = true)
    private String name;
    @XmlElement(namespace = "https://interoperabilnost.hr/sport", required = true)
    private String slug;
    public SportType() {}
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
}

