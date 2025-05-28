package hr.algebra.server.controller;

import hr.algebra.server.model.SportType;
import hr.algebra.server.model.Sports;
import hr.algebra.server.service.OddsApiService;
import hr.algebra.server.soap.SearchResponse;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/jaxb")
@RequiredArgsConstructor
public class JaxbValidationController {

    private final OddsApiService oddsApiService;

    @GetMapping("/validate")
    public List<String> validateSportsXml() {
        List<String> errors = new ArrayList<>();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Sports.class, SportType.class);
            Unmarshaller tito = jaxbContext.createUnmarshaller();

            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(getClass().getClassLoader().getResource("schemes/sport.xsd"));
            tito.setSchema(schema);

            tito.setEventHandler(event -> {
                errors.add(event.getMessage());
                return true;
            });

            Resource resource = new ClassPathResource("schemes/xml/sports.xml");
            tito.unmarshal(resource.getInputStream());
        } catch (JAXBException | SAXException | IOException e) {
            errors.add("Greška: " + e.getMessage());
        }
        return errors.isEmpty() ? List.of("XML je validan prema XSD shemi") : errors;
    }
}
