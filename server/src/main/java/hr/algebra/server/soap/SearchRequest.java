package hr.algebra.server.soap;

import jakarta.xml.bind.annotation.*;
import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "term" })
@XmlRootElement(name = "SearchRequest", namespace = "https://interoperabilnost.hr/sport")
public class SearchRequest {

    @XmlElement(namespace = "https://interoperabilnost.hr/sport", required = true)
    private String term;
}
