package hr.algebra.server.factory;


import hr.algebra.server.model.SportType;
import hr.algebra.server.soap.SearchRequest;
import hr.algebra.server.soap.SearchResponse;
import jakarta.xml.bind.annotation.XmlRegistry;
import lombok.NoArgsConstructor;

@XmlRegistry
@NoArgsConstructor
public class ObjectFactory {

    public SearchRequest createSearchRequest() {
        return new SearchRequest();
    }

    public SearchResponse createSearchResponse() {
        return new SearchResponse();
    }

    public SportType createSportType() {
        return new SportType();
    }
}
