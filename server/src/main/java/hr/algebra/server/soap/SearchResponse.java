package hr.algebra.server.soap;

import hr.algebra.server.model.SportType;
import jakarta.xml.bind.annotation.*;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "sport" })
@XmlRootElement(name = "SearchResponse", namespace = "https://interoperabilnost.hr/sport")
public class SearchResponse {

    @XmlElement(name = "Sport", namespace = "https://interoperabilnost.hr/sport")
    private List<SportType> sport;

    public List<SportType> getSport() {
        if (sport == null) {
            sport = new ArrayList<>();
        }
        return sport;
    }
}