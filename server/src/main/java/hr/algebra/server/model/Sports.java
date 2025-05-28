package hr.algebra.server.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Sports", namespace = "https://interoperabilnost.hr/sport")
@XmlAccessorType(XmlAccessType.FIELD)
public class Sports {
    @XmlElement(name = "Sport", namespace = "https://interoperabilnost.hr/sport")
    private List<SportType> sport = new ArrayList<>();
    public Sports() {}
    public List<SportType> getSport() { return sport; }
    public void setSport(List<SportType> sport) { this.sport = sport; }
}
