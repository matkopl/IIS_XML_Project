package hr.algebra.server.controller;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import hr.algebra.server.handler.XmlErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/sport")
public class XsdRngController {

    @PostMapping(value = "/xsd", consumes = "application/xml")
    public ResponseEntity<String> uploadSportXsd(@RequestBody String xml) {
        try {
            var schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            var schema = schemaFactory.newSchema(getClass().getResource("/schemes/sport.xsd"));
            var validator = schema.newValidator();

            XmlErrorHandler errorHandler = new XmlErrorHandler();
            validator.setErrorHandler(errorHandler);
            validator.validate(new StreamSource(new StringReader(xml)));

            if (!errorHandler.getExceptions().isEmpty()) {
                StringBuilder sb = new StringBuilder("Greške u XSD validaciji:\n");
                for (SAXParseException exception : errorHandler.getExceptions()) {
                    sb.append("- ").append(exception.getMessage()).append("\n");
                }
                return ResponseEntity.badRequest().body(sb.toString());
            }

            saveXmlToFile(xml, "server/src/main/resources/schemes/xml/saved_xsd.xml");
            return ResponseEntity.ok().body("Validan XML prema XSD shemi. XML je spremljen.");
        } catch (SAXException e) {
            return ResponseEntity.badRequest().body("Greška u XSD validaciji: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Greška pri XSD validaciji: " + e.getMessage());
        }
    }

    @PostMapping(value = "/rng", consumes = "application/xml")
    public ResponseEntity<String> uploadSportRng(@RequestBody String xml) {
        try {
            var rngSchema = getClass().getResourceAsStream("/schemes/sport.rng");
            XmlErrorHandler errorHandler = new XmlErrorHandler();
            PropertyMapBuilder builder = new PropertyMapBuilder();
            builder.put(ValidateProperty.ERROR_HANDLER, errorHandler);

            ValidationDriver driver = new ValidationDriver(builder.toPropertyMap());
            driver.loadSchema(new org.xml.sax.InputSource(rngSchema));
            boolean valid = driver.validate(new org.xml.sax.InputSource(new StringReader(xml)));

            StringBuilder sb = new StringBuilder();

            if (!errorHandler.getExceptions().isEmpty() || !valid) {
                sb.append("Greške u RNG validaciji:\n");
                for (SAXParseException exception : errorHandler.getExceptions()) {
                    sb.append("- ").append(exception.getMessage()).append("\n");
                }
                if (errorHandler.getExceptions().isEmpty() && !valid) {
                    sb.append("- XML nije validan.\n");
                }
                return ResponseEntity.badRequest().body(sb.toString());
            }

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList nameNodes = doc.getElementsByTagNameNS("https://interoperabilnost.hr/sport", "name");
            NodeList slugNodes = doc.getElementsByTagNameNS("https://interoperabilnost.hr/sport", "slug");

            Set<String> names = new HashSet<>();
            Set<String> slugs = new HashSet<>();
            boolean hasDuplicate = false;

            sb.append("Greške u RNG validaciji:\n");
            boolean hasError = false;
            for (int i = 0; i < nameNodes.getLength(); i++) {
                String name = nameNodes.item(i).getTextContent();
                String slug = slugNodes.item(i).getTextContent();
                if (name == null || name.isBlank()) {
                    sb.append("- 'name' ne smije biti prazan (Sport ").append(i + 1).append(")\n");
                    hasError = true;
                }
                if (slug == null || slug.isBlank()) {
                    sb.append("- 'slug' ne smije biti prazan (Sport ").append(i + 1).append(")\n");
                    hasError = true;
                }
                if (!names.add(name)) {
                    sb.append("- 'name' nije jedinstven (Sport ").append(i + 1).append(")\n");
                    hasDuplicate = true;
                }
                if (!slugs.add(slug)) {
                    sb.append("- 'slug' nije jedinstven (Sport ").append(i + 1).append(")\n");
                    hasDuplicate = true;
                }
            }
            if (hasError || hasDuplicate) {
                return ResponseEntity.badRequest().body(sb.toString());
            }

            saveXmlToFile(xml, "server/src/main/resources/schemes/xml/saved_rng.xml");
            return ResponseEntity.ok().body("Validan XML prema RNG shemi. XML je spremljen.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Greška pri RNG validaciji: " + e.getMessage());
        }
    }

    private void saveXmlToFile(String xml, String filePath) throws IOException {
        Files.writeString(Path.of(filePath), xml, StandardCharsets.UTF_8);
    }
}
